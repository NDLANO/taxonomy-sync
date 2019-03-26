package no.ndla.taxonomysync.services

import no.ndla.taxonomysync.domain.CopyReport
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Types
import javax.sql.DataSource

const val BATCH_SIZE = 1000

@Service
class DatabaseCopierService(@Qualifier("source") val sourceDatabase: DataSource,
                            @Qualifier("target") val targetDatabase: DataSource,
                            val sourceTemplate: JdbcTemplate = JdbcTemplate(sourceDatabase),
                            val targetTemplate: JdbcTemplate = JdbcTemplate(targetDatabase)) {

    private final val tables = arrayOf("subject", "topic", "resource", "filter", "filter_translation", "relevance",
            "relevance_translation", "resource_filter", "resource_type", "resource_resource_type", "resource_translation",
            "resource_type_translation", "schema_version", "subject_topic", "subject_translation", "topic_filter",
            "topic_resource", "topic_subtopic", "topic_translation", "url_map")

    private final val sequences = arrayOf("filter_id_seq", "filter_translation_id_seq", "relevance_id_seq",
            "relevance_translation_id_seq", "resource_filter_id_seq", "resource_id_seq", "resource_resource_type_id_seq",
            "resource_translation_id_seq", "resource_type_id_seq", "resource_type_translation_id_seq", "subject_id_seq",
            "subject_topic_id_seq", "subject_translation_id_seq", "topic_filter_id_seq", "topic_id_seq",
            "topic_resource_id_seq", "topic_subtopic_id_seq", "topic_translation_id_seq")

    fun copySourceToTarget(): CopyReport {
        val report = CopyReport()
        truncateTargetTables(report)
        copyTables(report)
        copySequences(report)
        return report
    }

    private fun truncateTargetTables(report: CopyReport) {
        tables.reversed().forEach { table ->
            report.log.add("Truncating target table: $table")
            targetTemplate.execute("truncate table $table cascade;")
        }
    }

    private fun copyTables(report: CopyReport) {
        tables.forEach { table ->
            var rowCount = 0
            val values = arrayListOf<String>()
            report.log.add("Processing table: $table...")
            sourceTemplate.query("select * from $table") { resultSet: ResultSet ->
                val metaData = resultSet.metaData
                var valuesString = ""
                for (index in 1..metaData.columnCount) {
                    valuesString += stringifyColumnValue(metaData, index, resultSet)
                    if (index < metaData.columnCount) {
                        valuesString += ","
                    }
                }
                values.add("($valuesString)")
                rowCount++
                if (values.size == BATCH_SIZE) {
                    insertValues(table, values)
                    values.clear()
                }
            }
            insertValues(table, values)
            report.log.add("$rowCount rows copied")
        }
    }

    private fun copySequences(report: CopyReport) {
        sequences.forEach { sequence ->
            val table = sequence.substring(0, sequence.indexOf("_id"))
            val maxId = sourceTemplate.queryForObject("select max(id) from $table", Int::class.java) ?: 1
            report.log.add("Setting sequence $sequence to $maxId")
            targetTemplate.execute("select setval('$sequence', $maxId);")
        }
    }

    private fun stringifyColumnValue(metaData: ResultSetMetaData, columnIndex: Int, resultSet: ResultSet): Any? {
        return when (metaData.getColumnType(columnIndex)) {
            Types.VARCHAR -> convertColumnToString(resultSet, columnIndex)
            Types.TIMESTAMP -> convertColumnToTimestampFunction(resultSet, columnIndex)
            else -> resultSet.getObject(columnIndex)
        }
    }

    private fun convertColumnToTimestampFunction(resultSet: ResultSet, columnIndex: Int): String? {
        val timestamp = resultSet.getTimestamp(columnIndex)
        return if (timestamp == null) null else "to_timestamp('$timestamp', 'YYYY-MM-DD TT:MI:SS.US')"
    }

    private fun convertColumnToString(resultSet: ResultSet, columnIndex: Int): String? {
        val value = resultSet.getString(columnIndex)
        return if (value == null) null else "'${escapeSingleQuotes(value)}'"
    }

    private fun escapeSingleQuotes(value: String): String {
        return value.replace("'", "''")
    }

    private fun insertValues(table: String, values: ArrayList<String>) {
        if (values.size > 0) {
            var insert = "insert into $table values "
            values.forEachIndexed { index, value ->
                insert += value
                insert += (if (index < values.size - 1) ", " else ";")
            }
            targetTemplate.update(insert)
        }
    }
}