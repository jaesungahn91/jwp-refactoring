package kitchenpos.application;

import kitchenpos.dao.MenuRepository;
import kitchenpos.dao.OrderLineItemRepository;
import kitchenpos.dao.OrderRepository;
import kitchenpos.dao.OrderTableRepository;
import kitchenpos.domain.*;
import kitchenpos.dto.request.OrderLineItemRequest;
import kitchenpos.dto.request.OrderRequest;
import kitchenpos.dto.response.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderLineItemRepository orderLineItemRepository;
    @Mock
    private OrderTableRepository orderTableRepository;

    private Order 주문;
    private OrderTable 테이블;
    private OrderTable 빈_테이블;
    private OrderLineItem 주문내역;

    @BeforeEach
    void setUp() {
        주문 = new Order(1L, 1L);
        테이블 = new OrderTable(1L, new TableGroup(1L), 1, false);
        빈_테이블 = new OrderTable(2L, new TableGroup(2L), 0, true);
        주문내역 = new OrderLineItem(1L, 주문, 1L, 1);
    }

    @DisplayName("주문 생성")
    @Test
    void create() {
        // given
        List<OrderLineItemRequest> orderLineItemRequests = Collections.singletonList(new OrderLineItemRequest(1L, 1));
        OrderRequest request = new OrderRequest(1L, orderLineItemRequests);
        given(orderRepository.save(any())).willReturn(주문);
        given(menuRepository.countByIdIn(any())).willReturn(1L);
        given(orderTableRepository.findById(any())).willReturn(Optional.of(테이블));

        // when
        OrderResponse response = orderService.create(request);

        // then
        assertAll(
                () -> assertThat(response.getId()).isEqualTo(1L),
                () -> assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COOKING.name())
        );
    }

    @DisplayName("주문 목록 조회")
    @Test
    void list() {
        // given
        주문.addOrderLineItems(Collections.singletonList(주문내역));
        given(orderRepository.findAll()).willReturn(Collections.singletonList(주문));

        // when
        List<OrderResponse> responses = orderService.list();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.stream().map(OrderResponse::getId)).containsExactly(1L),
                () -> assertThat(responses.get(0).getOrderLineItems()).hasSize(1)
        );
    }

    @DisplayName("주문상태 변경")
    @Test
    void changeOrderStatus() {
        // given
        주문.addOrderLineItems(Collections.singletonList(주문내역));
        given(orderRepository.findById(any())).willReturn(Optional.of(주문));

        // when
        OrderResponse response = orderService.changeOrderStatus(1L, OrderStatus.MEAL);

        // then
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.MEAL.name());
    }

    @Test
    void 주문의_개수가_0개인_경우() {
        // given
        OrderRequest request = new OrderRequest(1L, Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문_내역의_건수가_주문한_메뉴들의_수와_같지_않은경우() {
        // given
        List<OrderLineItemRequest> orderLineItemRequests = Collections.singletonList(new OrderLineItemRequest(1L, 1));
        OrderRequest request = new OrderRequest(1L, orderLineItemRequests);
        given(menuRepository.countByIdIn(any())).willReturn(3L);

        // when & then
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 빈_테이블에서_주문한_경우() {
        // given
        List<OrderLineItemRequest> orderLineItemRequests = Collections.singletonList(new OrderLineItemRequest(1L, 1));
        OrderRequest request = new OrderRequest(1L, orderLineItemRequests);
        given(menuRepository.countByIdIn(any())).willReturn(1L);
        given(orderTableRepository.findById(1L)).willReturn(Optional.of(빈_테이블));


        // when & then
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문상태가_완료인_경우에_상태_변경을_요청한_경우() {
        // given
        주문.changeOrderStatus(OrderStatus.COMPLETION);
        given(orderRepository.findById(any())).willReturn(Optional.of(주문));

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, OrderStatus.MEAL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
