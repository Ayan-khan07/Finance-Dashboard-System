package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.RecordRequest;
import com.finance.dashboard.dto.response.PageResponse;
import com.finance.dashboard.dto.response.RecordResponse;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.mapper.RecordMapper;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.FinancialRecordSpecifications;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.util.PageableSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordService {

    private static final List<String> SORTABLE_FIELDS = List.of("id", "amount", "type", "category", "date");

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<RecordResponse> list(String category,
                                             RecordType type,
                                             LocalDate startDate,
                                             LocalDate endDate,
                                             Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must not be after endDate");
        }
        Pageable safe = PageableSanitizer.sanitize(pageable, SORTABLE_FIELDS, Sort.by(Sort.Direction.DESC, "date"));

        Page<FinancialRecord> page = recordRepository.findAll(
                FinancialRecordSpecifications.withFilters(category, type, startDate, endDate), safe);
        return PageResponse.from(page.map(RecordMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public RecordResponse get(Long id) {
        return RecordMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public RecordResponse create(RecordRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + creatorId + " not found"));

        FinancialRecord record = FinancialRecord.builder()
                .user(creator)
                .amount(request.amount().setScale(2, RoundingMode.HALF_UP))
                .type(request.type())
                .category(request.category().trim())
                .date(request.date())
                .notes(normalizeNotes(request.notes()))
                .build();

        return RecordMapper.toResponse(recordRepository.save(record));
    }

    /** Full replace of the editable fields; the owner stays the original creator. */
    @Transactional
    public RecordResponse update(Long id, RecordRequest request) {
        FinancialRecord record = findOrThrow(id);
        record.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        record.setType(request.type());
        record.setCategory(request.category().trim());
        record.setDate(request.date());
        record.setNotes(normalizeNotes(request.notes()));
        return RecordMapper.toResponse(record);   // flushed by dirty checking on commit
    }

    @Transactional
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Financial record with id " + id + " not found");
        }
        recordRepository.deleteById(id);
    }

    private FinancialRecord findOrThrow(Long id) {
        return recordRepository.findWithUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record with id " + id + " not found"));
    }

    private static String normalizeNotes(String notes) {
        return StringUtils.hasText(notes) ? notes.trim() : null;
    }
}
