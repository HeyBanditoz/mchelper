package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Requires(database = true)
public class SqlCommand extends ElevatedCommand {
    @Override
    public String commandName() {
        return "sql";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<sql query>")
                .withDescription("Evaluates SQL.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        try (Connection c = ce.getDatabase().getConnection()) {
            ResultSet rs = c.prepareStatement(ce.getCommandArgsString()).executeQuery();
            ce.sendReply(formatTable(rs));
            rs.close();
        }
        return Status.SUCCESS;
    }

    /**
     * Builds a pretty(ish) printed String of the current ResultSet.
     *
     * @param rs The ResultSet to build to a String.
     * @return The built String
     * @throws SQLException If something went wrong while fetching SQL data.
     * @author https://gist.github.com/mikbuch/299568988fa7997cb28c7c84309232b1
     */
    private String formatTable(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder("```");
        // Prepare metadata object and get the number of columns.
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        // Print column names (a header).
        for (int i = 1; i <= columnsNumber; i++) {
            if (i > 1) {
                sb.append(" | ");
            }
            sb.append(rsmd.getColumnName(i));
        }
        sb.append('\n');
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) {
                    sb.append(" | ");
                }
                sb.append(rs.getString(i));
            }
            sb.append('\n');
        }
        sb.append("```");
        return sb.toString();
    }
}
