package nextstep.subway.common;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Random;

public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    private final int slaveSize;
    public static final String DATASOURCE_KEY_MASTER = "master";
    public static final String DATASOURCE_KEY_SLAVE = "slave";

    public ReplicationRoutingDataSource(final int slaveSize) {
        this.slaveSize = slaveSize;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        return (isReadOnly) ? DATASOURCE_KEY_MASTER : String.format("%s-%d", DATASOURCE_KEY_SLAVE, hash()) ;
    }

    private int hash() {
        return Math.abs(new Random().nextInt()) % slaveSize;
    }

}
