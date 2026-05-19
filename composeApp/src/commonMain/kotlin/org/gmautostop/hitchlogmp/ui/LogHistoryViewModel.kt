package org.gmautostop.hitchlogmp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.domain.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.formatDateLocale
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.history.DateGroupUi
import org.gmautostop.hitchlogmp.ui.history.LogHistoryData
import org.gmautostop.hitchlogmp.ui.history.LogHistoryEntryUi
import org.gmautostop.hitchlogmp.ui.history.RecordGroupUi
import org.gmautostop.hitchlogmp.ui.history.SortMode
import org.gmautostop.hitchlogmp.ui.history.computeRecordVersions

class LogHistoryViewModel(
    private val repository: Repository,
    private val logId: String
) : ViewModel() {

    val state: StateFlow<ViewState<LogHistoryData>>
        field = MutableStateFlow<ViewState<LogHistoryData>>(ViewState.Loading)

    val sortMode: StateFlow<SortMode>
        field = MutableStateFlow(SortMode.BY_RECORD)

    fun setSortMode(mode: SortMode) {
        sortMode.value = mode
    }

    init {
        viewModelScope.launch {
            state.value = ViewState.Loading

            repository.getLogHistory(logId)
                .distinctUntilChanged()
                .collect { response ->
                    state.value = when (response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Failure -> ViewState.Error(response.error)
                        is Response.Success -> ViewState.Show(buildLogHistoryData(response.data))
                    }
                }
        }
    }

    private fun buildLogHistoryData(rawData: List<Pair<String, HitchLogRecordHistoryEntry>>): LogHistoryData {
        val grouped: Map<String, List<HitchLogRecordHistoryEntry>> = rawData
            .groupBy { (recordId, _) -> recordId }
            .mapValues { (_, pairs) -> pairs.map { it.second } }

        val byRecordGroups = grouped.map { (recordId, entries) ->
            val sorted = entries.sortedBy { it.editedAt }
            val liveEntry = sorted.last()
            val liveType = liveEntry.type
            val liveText = liveEntry.text
            val isDeleted = liveEntry.changeType == ChangeType.DELETE
            val originalTime = sorted.firstOrNull { it.changeType == ChangeType.CREATE }?.time

            val versions = computeRecordVersions(sorted)
            val logHistoryEntries = versions.map { version ->
                LogHistoryEntryUi(
                    historyId = version.historyId,
                    editedAt = version.editedAt,
                    formattedEditedAt = version.formattedEditedAt,
                    recordId = recordId,
                    changeType = version.changeType,
                    recordType = liveType,
                    before = version.before,
                    after = version.after
                )
            }

            RecordGroupUi(
                recordId = recordId,
                liveType = liveType,
                formattedOriginalTime = originalTime?.let { timeFormatForDisplay.format(it) },
                liveText = liveText,
                isDeleted = isDeleted,
                entries = logHistoryEntries
            )
        }.sortedBy { group ->
            grouped[group.recordId]?.minByOrNull { it.editedAt }?.editedAt
        }

        val allEntries = byRecordGroups.flatMap { it.entries }.sortedBy { it.editedAt }

        val byTimeGroups = allEntries
            .groupBy { it.editedAt.localTZDateTime().date }
            .entries
            .sortedBy { it.key }
            .map { (date, entries) ->
                DateGroupUi(
                    dateLabel = formatDateLocale(date),
                    date = date,
                    entries = entries
                )
            }

        return LogHistoryData(
            byTimeGroups = byTimeGroups,
            byRecordGroups = byRecordGroups
        )
    }

}
