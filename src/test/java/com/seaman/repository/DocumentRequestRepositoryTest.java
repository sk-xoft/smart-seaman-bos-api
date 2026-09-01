package com.seaman.repository;

import com.seaman.model.request.DocumentInspectionItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRequestRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate template;

    private DocumentRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DocumentRequestRepository();
        ReflectionTestUtils.setField(repository, "template", template);
    }

    @Test
    void updateInspectionResultsCountsAnUnchangedExistingItemAsProcessed() {
        DocumentInspectionItemRequest inspection = new DocumentInspectionItemRequest();
        inspection.setSortOrder(1);
        inspection.setCheckResult("fix");
        inspection.setCheckNote("tt");

        when(template.queryForObject(
                anyString(),
                any(MapSqlParameterSource.class),
                eq(Integer.class)
        )).thenReturn(1);
        when(template.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(0);

        int processedRows = repository.updateInspectionResults("MOCK-DR-20260831-04", List.of(inspection));

        assertEquals(1, processedRows);
        verify(template).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    void findAllSortsCancelledRequestsByLatestCancellationTime() {
        when(template.queryForList(anyString(), any(MapSqlParameterSource.class))).thenReturn(List.of());

        repository.findAll(0, 20, "ยกเลิก", null, null, null);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).queryForList(sqlCaptor.capture(), any(MapSqlParameterSource.class));
        assertTrue(sqlCaptor.getValue().contains(
                "ORDER BY cancelled_at DESC, dr.submitted_at DESC, dr.created_at DESC"
        ));
    }
}