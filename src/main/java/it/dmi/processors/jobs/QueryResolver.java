package it.dmi.processors.jobs;

import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.structure.internal.QueryType;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.update.Update;
import org.jetbrains.annotations.NotNull;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
public class QueryResolver {

    public static @NotNull QueryType resolveQuery(String query) throws JSQLParserException, IllegalArgumentException {
        if(isBlank(query)) throw new IllegalArgumentException("Query null or empty.");
        Statement stmt = CCJSqlParserUtil.parse(query);
        if (stmt instanceof Select select) {
            if (select instanceof SetOperationList setOperationList) {
                for (Select listSelects : setOperationList.getSelects())
                    if (isCountQuery(listSelects)) return QueryType.SELECT_COUNT;
                return QueryType.SELECT;
            }
            if (isCountQuery(select)) return QueryType.SELECT_COUNT;
            return QueryType.SELECT;
        }
        if (stmt instanceof Insert) return QueryType.INSERT;
        if (stmt instanceof Update) return QueryType.UPDATE;
        if (stmt instanceof Delete) return QueryType.DELETE;
        log.error("Not a valid or supported query.");
        throw new IllegalArgumentException("Not a valid or supported query.");
    }

    private static boolean isCountQuery(Select select) {
        if (select instanceof PlainSelect plainSelect)
            for (SelectItem<?> selectItem : plainSelect.getSelectItems())
                if (selectItem.getExpression() instanceof Function function)
                    if (function.getName().equalsIgnoreCase("count")) return true;
        return false;
    }

    public static boolean acceptSelect(@NotNull ConfigurazioneDTO c) {
        try {
            return resolveQuery(c.sqlScript()) == QueryType.SELECT;
        } catch (JSQLParserException e) {
            log.error("Could not resolve query to a supported type.");
            throw new RuntimeException(e);
        }
    }

    public static boolean acceptCount(@NotNull ConfigurazioneDTO c) {
        try {
            return resolveQuery(c.sqlScript()) == QueryType.SELECT_COUNT;
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }
}
