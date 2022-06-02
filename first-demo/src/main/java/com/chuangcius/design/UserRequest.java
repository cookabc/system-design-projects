package com.chuangcius.design;

/**
 * UserRequest
 *
 * @author xugang.song
 * @date 2022.05.31
 */
public class UserRequest {
    private Long userId;
    private Long orderId;
    private Integer count;

    public UserRequest(Long userId, Long orderId, Integer count) {
        this.userId = userId;
        this.orderId = orderId;
        this.count = count;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "userId='" + userId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", count=" + count +
                '}';
    }
}
