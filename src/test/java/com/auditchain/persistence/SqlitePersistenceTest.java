package com.auditchain.persistence;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SqlitePersistenceTest {
  @Test void emptyStoreIsEmpty() {
    SqliteAuditStore store = SqliteAuditStore.inMemory();
    assertEquals(0, store.size());
    store.close();
  }
}
