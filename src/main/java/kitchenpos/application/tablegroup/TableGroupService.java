package kitchenpos.application.tablegroup;

import kitchenpos.domain.table.OrderTables;
import kitchenpos.domain.tablegroup.TableGroup;
import kitchenpos.domain.tablegroup.TableGroupRepository;
import kitchenpos.domain.tablegroup.TableGroupValidator;
import kitchenpos.dto.tablegroup.TableGroupDto;
import kitchenpos.event.tablegroup.GroupingOrderTableEvent;
import kitchenpos.event.tablegroup.UngroupOrderTableEvent;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TableGroupService {
    private final TableGroupRepository tableGroupRepository;
    private final TableGroupValidator tableGroupValidator;
    private final ApplicationEventPublisher eventPublisher;
    
    public TableGroupService(
        final TableGroupRepository tableGroupRepository,
        final TableGroupValidator tableGroupValidator,
        final ApplicationEventPublisher eventPublisher
    ) {
        this.tableGroupRepository = tableGroupRepository;
        this.tableGroupValidator = tableGroupValidator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TableGroupDto create(final TableGroupDto tableGroupDto) {
        final OrderTables validatedOrderTables = tableGroupValidator.getValidatedOrderTables(tableGroupDto);
        final List<Long> orderTableIds = validatedOrderTables.getOrderTableIds();
        
        TableGroup savedTableGroup = tableGroupRepository.save(TableGroup.of());
        
        eventPublisher.publishEvent(new GroupingOrderTableEvent(savedTableGroup.getId(), orderTableIds));

        return TableGroupDto.of(savedTableGroup, validatedOrderTables);
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        OrderTables validatedOrderTables = tableGroupValidator.getComplateOrderTable(tableGroupId);
        final List<Long> orderTableIds = validatedOrderTables.getOrderTableIds();

        eventPublisher.publishEvent(new UngroupOrderTableEvent(tableGroupId, orderTableIds));
    }
}
