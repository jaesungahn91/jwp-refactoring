package kitchenpos.table.application;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kitchenpos.exception.AppException;
import kitchenpos.exception.ErrorCode;
import kitchenpos.order.domain.OrderRepository;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table.dto.TableEmptyUpdateRequest;
import kitchenpos.table.dto.TableGuestsUpdateRequest;
import kitchenpos.table.dto.TableRequest;
import kitchenpos.table.dto.TableResponse;

@Service
@Transactional
public class TableService {
	private final OrderRepository orderRepository;
	private final OrderTableRepository orderTableRepository;

	public TableService(final OrderRepository orderRepository, final OrderTableRepository orderTableRepository) {
		this.orderRepository = orderRepository;
		this.orderTableRepository = orderTableRepository;
	}

	public TableResponse create(final TableRequest request) {
		OrderTable orderTable = request.toEntity();
		return new TableResponse(orderTableRepository.save(orderTable));
	}

	@Transactional(readOnly = true)
	public List<TableResponse> list() {
		return orderTableRepository.findAll().stream().map(TableResponse::new).collect(Collectors.toList());
	}

	public TableResponse changeEmpty(final Long orderTableId, final TableEmptyUpdateRequest request) {
		OrderTable orderTable = getNotGroupedOrderTableById(orderTableId);
		if (orderRepository.existsByOrderTableAndOrderStatusIn(orderTable,
			Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL))) {
			throw new AppException(ErrorCode.WRONG_INPUT, "빈 테이블 변경 시, 조리 중이거나 식사 중인 테이블이면 안됩니다");
		}
		orderTable.changeEmptyStatus(request.isEmpty());
		return new TableResponse(orderTable);
	}

	private OrderTable getNotGroupedOrderTableById(Long id) {
		OrderTable orderTable = orderTableRepository.findById(id)
			.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "주문 테이블을 찾을 수 없습니다"));
		if (Objects.nonNull(orderTable.getTableGroup())) {
			throw new AppException(ErrorCode.WRONG_INPUT, "테이블 그룹에 속해 있지 않아야 합니다");
		}
		return orderTable;
	}

	public TableResponse changeNumberOfGuests(final Long orderTableId, final TableGuestsUpdateRequest request) {
		final OrderTable orderTable = orderTableRepository.findById(orderTableId)
			.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "주문 테이블을 찾을 수 없습니다"));
		orderTable.changeNumberOfGuests(request.getNumberOfGuests());
		return new TableResponse(orderTable);
	}
}
