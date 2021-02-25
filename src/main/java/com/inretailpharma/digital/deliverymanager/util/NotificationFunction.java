package com.inretailpharma.digital.deliverymanager.util;



@FunctionalInterface
public interface NotificationFunction<T1, T2, T3, T4> {

    boolean sendNotification(T1 t1, T2 t2, T3 t3, T4 t4);

}
