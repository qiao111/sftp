#!/bin/sh

/opt/restart.sh payment_{0}
sed -i 's/FLIGHT_PAY_NOTIFY_URL=.*$/FLIGHT_PAY_NOTIFY_URL=http:\/\/192.168.0.223\/clutter\/payment\/flight\/payCallback.do/g' `grep -E 'FLIGHT_PAY_NOTIFY_URL=.*$' -rl /var/www/webapps/pet_payment_{0}/WEB-INF/classes/payment.properties`
sed -i 's/FLIGHT_REFUND_NOTIFY_URL=.*$/FLIGHT_REFUND_NOTIFY_URL=http:\/\/192.168.0.223\/clutter\/payment\/flight\/refundCallback.do/g' `grep -E 'FLIGHT_REFUND_NOTIFY_URL=.*$' -rl /var/www/webapps/pet_payment_{0}/WEB-INF/classes/payment.properties`

sed -i 's/TRAIN_PAY_NOTIFY_URL=.*$/TRAIN_PAY_NOTIFY_URL=http:\/\/192.168.0.223:6060\/clutter\/payment\/train\/payCallback.do/g' `grep -E 'TRAIN_PAY_NOTIFY_URL=.*$' -rl /var/www/webapps/pet_payment_{0}/WEB-INF/classes/payment.properties`
sed -i 's/TRAIN_REFUND_NOTIFY_URL=.*$/TRAIN_REFUND_NOTIFY_URL=http:\/\/192.168.0.223:6060\/clutter\/payment\/train\/refundCallback.do/g' `grep -E 'TRAIN_REFUND_NOTIFY_URL=.*$' -rl /var/www/webapps/pet_payment_{0}/WEB-INF/classes/payment.properties`
tail -f /opt/apache-tomcat-payment_{0}/logs/catalina.out
