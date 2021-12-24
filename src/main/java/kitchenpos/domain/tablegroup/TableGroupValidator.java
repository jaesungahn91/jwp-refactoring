package kitchenpos.domain.tablegroup;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kitchenpos.application.order.OrderService;
import kitchenpos.application.table.TableService;
import kitchenpos.domain.table.OrderTable;
import kitchenpos.domain.table.OrderTables;
import kitchenpos.dto.table.OrderTableDto;
import kitchenpos.dto.tablegroup.TableGroupDto;
import kitchenpos.exception.order.HasNotCompletionOrderException;
import kitchenpos.exception.table.HasOtherTableGroupException;
import kitchenpos.exception.table.NotEmptyOrderTableException;
import kitchenpos.exception.table.NotGroupingOrderTableCountException;
import kitchenpos.exception.table.NotRegistedMenuOrderTableException;

@Component
public class TableGroupValidator {
    private final OrderService orderService;
    private final TableService tableService;

    public TableGroupValidator(
        final OrderService orderService,
        final TableService tableService
    ) {
        this.orderService = orderService;
        this.tableService = tableService;
    }

    public void validateForUnGroup(OrderTables orderTables) {
        if (orderService.hasNotComplateStatus(orderTables.getOrderTableIds())) {
            throw new HasNotCompletionOrderException("계산완료가 되지않은 주문이 존재합니다.");
        }
    }

    public OrderTables getComplateOrderTable(Long tableGroupId) {
        final OrderTables orderTables = OrderTables.of(tableService.findByTableGroupId(tableGroupId));

        this.validateForUnGroup(orderTables);

        return orderTables;
    }

    public OrderTables getValidatedOrderTables(TableGroupDto tableGroup) {
        final List<Long> orderTableIds = tableGroup.getOrderTables().stream()
                                                    .map(OrderTableDto::getId)
                                                    .collect(Collectors.toList());

        final OrderTables savedOrderTables = OrderTables.of(tableService.findAllByIdIn(orderTableIds));

        checkAllExistOfOrderTables(tableGroup.getOrderTables(), savedOrderTables);

        checkOrderTableSize(savedOrderTables);

        for (int index = 0; index < savedOrderTables.size(); index++) {
            checkHasTableGroup(savedOrderTables.get(index));
            checkNotEmptyTable(savedOrderTables.get(index));
        }

        return savedOrderTables;
    }
    

    private static void checkHasTableGroup(final OrderTable orderTable) {
        if (orderTable.hasTableGroup()) {
            throw new HasOtherTableGroupException("단체지정이 된 주문테이블입니다.");
        }
    }

    private static void checkNotEmptyTable(final OrderTable orderTable) {
        if (!orderTable.isEmpty()) {
            throw new NotEmptyOrderTableException("주문테이블이 빈테이블이 아닙니다.");
        }
    }
    
    private static void checkOrderTableSize(final OrderTables orderTables) {
        if (orderTables.size() < 2) {
            throw new NotGroupingOrderTableCountException("주문 테이블의 개수가 2개 미만입니다.");
        }
    }

    private void checkAllExistOfOrderTables(final List<OrderTableDto> orderTables, final OrderTables savedOrderTables) {
        if (orderTables.size() != savedOrderTables.size()) {
            throw new NotRegistedMenuOrderTableException("요청된 주문테이블 수와 조회된 주문테이블 수가 일치하지 않습니다.");
        }
    }
}
