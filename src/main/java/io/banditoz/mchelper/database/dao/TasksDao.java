package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;
import java.time.LocalDateTime;

import io.banditoz.mchelper.money.Task;

public interface TasksDao {
    LocalDateTime getWhenCanExecute(long id, Task t) throws SQLException;
    void putOrUpdateTask(long id, Task t) throws SQLException;
}
