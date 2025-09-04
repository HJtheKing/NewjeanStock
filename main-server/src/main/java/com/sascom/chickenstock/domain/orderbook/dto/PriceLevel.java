package com.sascom.chickenstock.domain.orderbook.dto;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PriceLevel {
    // 삽입 순서 유지 → FIFO
    private final LinkedHashMap<Long, Order> fifo = new LinkedHashMap<>();

    public void add(Order o) {
        fifo.put(o.orderId, o);
    }

    public Order peekFirst() {
        Iterator<Map.Entry<Long, Order>> it = fifo.entrySet().iterator();
        return it.hasNext() ? it.next().getValue() : null;
    }

    public Order pollFirst() {
        Iterator<Map.Entry<Long, Order>> it = fifo.entrySet().iterator();
        if (!it.hasNext()) return null;
        Map.Entry<Long, Order> e = it.next();
        it.remove();
        return e.getValue();
    }

    public boolean removeById(long orderId) {
        return fifo.remove(orderId) != null;
    }

    public boolean isEmpty() {
        return fifo.isEmpty();
    }

    public long totalQuantity() {
        long sum = 0L;
        for (Order o : fifo.values()) sum += o.remaining;
        return sum;
    }

    public Iterator<Order> iterator() {
        final Iterator<Map.Entry<Long, Order>> it = fifo.entrySet().iterator();
        return new Iterator<Order>() {
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public Order next() { return it.next().getValue(); }
        };
    }
}