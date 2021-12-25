package kitchenpos.order.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.AcceptanceTest;
import kitchenpos.order.dto.ChangeOrderStatusRequest;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.table.acceptance.step.TableAcceptStep;
import kitchenpos.table.dto.TableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static kitchenpos.order.acceptance.step.OrderAcceptStep.주문_등록_요청;
import static kitchenpos.order.acceptance.step.OrderAcceptStep.주문_등록_확인;
import static kitchenpos.order.acceptance.step.OrderAcceptStep.주문_목록_조회_요청;
import static kitchenpos.order.acceptance.step.OrderAcceptStep.주문_목록_조회_확인;
import static kitchenpos.order.acceptance.step.OrderAcceptStep.주문_상태_변경_요청;
import static kitchenpos.order.acceptance.step.OrderAcceptStep.주문_상태_변경_확인;

@DisplayName("주문 인수테스트")
class OrderAcceptTest extends AcceptanceTest {

    private TableResponse 테이블;

    @BeforeEach
    void setup() {
        테이블 = TableAcceptStep.테이블이_등록되어_있음(2, false);
    }

    @DisplayName("주문을 관리한다")
    @Test
    void 주문을_관리한다() {
        // given
        OrderLineItemRequest 주문_항목 = OrderLineItemRequest.of(1L, 1L);
        OrderRequest 등록_요청_데이터 = OrderRequest.of(테이블.getId(), Collections.singletonList(주문_항목));

        // when
        ExtractableResponse<Response> 주문_등록_응답 = 주문_등록_요청(등록_요청_데이터);

        // then
        OrderResponse 등록된_주문 = 주문_등록_확인(주문_등록_응답, 등록_요청_데이터);

        // when
        ExtractableResponse<Response> 주문_목록_조회_응답 = 주문_목록_조회_요청();

        // then
        주문_목록_조회_확인(주문_목록_조회_응답, 등록된_주문);

        // given
        ChangeOrderStatusRequest 상태_변경_요청_데이터 = ChangeOrderStatusRequest.of("MEAL");


        // when
        ExtractableResponse<Response> 주문_상태_변경_응답 = 주문_상태_변경_요청(주문_등록_응답, 상태_변경_요청_데이터);

        // then
        주문_상태_변경_확인(주문_상태_변경_응답, 상태_변경_요청_데이터);
    }
}
