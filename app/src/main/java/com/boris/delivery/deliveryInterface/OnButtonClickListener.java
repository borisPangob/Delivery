package com.boris.delivery.deliveryInterface;

public interface OnButtonClickListener {
    void onDeleteClick(int position);
    void onIncreaseClick(int position);
    void onDecreaseClick(int position);
    void onValidateClick(int position);

    void onDeclineClick(int position);
}
