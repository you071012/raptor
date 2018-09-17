package com.mo9.raptor.engine.state.handler.loan;

import com.mo9.raptor.engine.calculator.ILoanCalculator;
import com.mo9.raptor.engine.calculator.LoanCalculatorFactory;
import com.mo9.raptor.engine.entity.PayOrderEntity;
import com.mo9.raptor.engine.exception.MergeException;
import com.mo9.raptor.engine.exception.UnSupportTimeDiffException;
import com.mo9.raptor.engine.service.IPayOrderService;
import com.mo9.raptor.engine.simulator.ClockFactory;
import com.mo9.raptor.engine.state.action.IActionExecutor;
import com.mo9.raptor.engine.entity.LoanOrderEntity;
import com.mo9.raptor.engine.enums.StatusEnum;
import com.mo9.raptor.engine.state.action.impl.loan.EntryResponseAction;
import com.mo9.raptor.engine.state.event.IEvent;
import com.mo9.raptor.engine.exception.InvalidEventException;
import com.mo9.raptor.engine.state.event.impl.loan.LoanEntryEvent;
import com.mo9.raptor.engine.state.handler.IStateHandler;
import com.mo9.raptor.engine.state.handler.StateHandler;
import com.mo9.raptor.engine.state.launcher.IEventLauncher;
import com.mo9.raptor.engine.structure.Scheme;
import com.mo9.raptor.engine.structure.item.Item;
import com.mo9.raptor.exception.LoanEntryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Created by gqwu on 2018/4/4.
 */
@Component
@StateHandler(name = StatusEnum.LENT)
public class LentStateHandler implements IStateHandler<LoanOrderEntity> {

    @Autowired
    LoanCalculatorFactory loanCalculatorFactory;

    @Autowired
    IEventLauncher payEventLauncher;

    @Autowired
    private IPayOrderService payOrderService;

    @Override
    public LoanOrderEntity handle (LoanOrderEntity loanOrder, IEvent event, IActionExecutor actionExecutor)
            throws InvalidEventException, LoanEntryException {

        if (event instanceof LoanEntryEvent) {
            LoanEntryEvent loanEntryEvent = (LoanEntryEvent) event;
            Item entryItem = loanEntryEvent.getEntryItem();
            String payType = loanEntryEvent.getPayType();
            String payOrderId = loanEntryEvent.getPayOrderId();
            PayOrderEntity payOrderEntity = payOrderService.getByOrderId(payOrderId);
            ILoanCalculator loanCalculator = loanCalculatorFactory.load(loanOrder);
            Item realItem = loanCalculator.realItem(System.currentTimeMillis(), loanOrder);
            loanOrder = loanCalculator.itemEntry(loanOrder, payType, payOrderEntity.getPostponeDays(), realItem, entryItem);
            BigDecimal paid = entryItem.sum();
            // 还款合法, 则向还款订单发送入账反馈
            actionExecutor.append(new EntryResponseAction(loanEntryEvent.getPayOrderId(), paid, payEventLauncher));
        } else {
            throw new InvalidEventException("贷款订单状态与事件类型不匹配，状态：" + loanOrder.getStatus() + "，事件：" + event);
        }
        return loanOrder;
    }
}
