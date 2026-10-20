package io.fireflysoftware.common.batch.audit;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

/**
 * Repository for persisting BatchAuditRecord using Spring Data R2DBC mapping
 * without hand-written SQL.
 */
public class R2dbcBatchAuditRepository {

    private final R2dbcEntityTemplate template;

    public R2dbcBatchAuditRepository(ConnectionFactory connectionFactory) {
        this.template = new R2dbcEntityTemplate(connectionFactory);
    }

    public void insert(BatchAuditRecord record) {
        template.insert(record).block();
    }

    public void update(BatchAuditRecord record) {
        template.update(record).block();
    }
}
