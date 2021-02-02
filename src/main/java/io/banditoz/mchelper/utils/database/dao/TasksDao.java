package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.money.Task;

import java.sql.SQLException;
import java.time.LocalDateTime;

public interface TasksDao {
    LocalDateTime getWhenCanExecute(long id, Task t) throws SQLException;
    void putOrUpdateTask(long id, Task t) throws SQLException;
}
