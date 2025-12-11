package com.example.news.presentation.screen.subscriptions

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.query
import com.example.news.data.local.ArticlesDbModel
import com.example.news.domain.entity.Article
import com.example.news.domain.usecase.AddSubscriptionUseCase
import com.example.news.domain.usecase.ClearAllArticlesUseCase
import com.example.news.domain.usecase.GetAllSubscriptionsUseCase
import com.example.news.domain.usecase.GetArticlesByTopicUseCase
import com.example.news.domain.usecase.RemoveSubscriptionUseCase
import com.example.news.domain.usecase.UpdateSubscribedArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val clearAllArticlesUseCase: ClearAllArticlesUseCase,
    private val getAllSubscriptionsUseCase: GetAllSubscriptionsUseCase,
    private val getArticlesByTopicUseCase: GetArticlesByTopicUseCase,
    private val removeSubscriptionUseCase: RemoveSubscriptionUseCase,
    private val updateSubscribedArticlesUseCase: UpdateSubscribedArticlesUseCase
): ViewModel() {
    private val _state = MutableStateFlow(SubscriptionsState())
    val state = _state.asStateFlow()



    init {
        observeSubscriptions()
        observeSelectedTopics()
    }


    fun processCommand(command: SubscriptionsCommand) {
        when(command) {
            SubscriptionsCommand.ClearArticles -> {
                viewModelScope.launch {
                    val topics = _state.value.selectedTopics
                    clearAllArticlesUseCase(topics)
                }
            }
            SubscriptionsCommand.ClickSubscribe -> {
                viewModelScope.launch {
                    _state.update { state ->
                        val topic = _state.value.query.trim()
                        addSubscriptionUseCase(topic)
                        state.copy(
                            query = ""
                        )
                    }
                }
            }
            is SubscriptionsCommand.InputTopic -> {
                _state.update { state ->
                    state.copy(
                        query = command.query
                    )
                }
            }
            SubscriptionsCommand.RefreshData -> {
                viewModelScope.launch {
                    updateSubscribedArticlesUseCase()
                }
            }
            is SubscriptionsCommand.RemoveSubscription -> {
                viewModelScope.launch {
                    removeSubscriptionUseCase(command.topic)
                }
            }
            is SubscriptionsCommand.ToggleTopicSelection -> {
                _state.update { state ->
                    val subscriptions = state.subscriptions.toMutableMap()
                    val isSelected = subscriptions[command.topic] ?: false
                    subscriptions[command.topic] = !isSelected
                    state.copy(
                        subscriptions = subscriptions
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSelectedTopics() {
        _state.map {it.selectedTopics}.distinctUntilChanged()
            .flatMapLatest {
                getArticlesByTopicUseCase(it)
            }
            .onEach {
                _state.update { state ->
                    state.copy(
                        articles = it
                    )
                }
            }
            .launchIn(viewModelScope)
    }
    private fun observeSubscriptions() {
        getAllSubscriptionsUseCase()
            .onEach { subscriptions ->
                _state.update { state ->
                    val updatedTopics = subscriptions.associateWith { topic ->
                        state.subscriptions[topic] ?: true
                    }
                    state.copy(
                        subscriptions = updatedTopics
                    )
                }
            }.launchIn(viewModelScope)
    }
}



sealed interface SubscriptionsCommand {
    data class InputTopic(val query: String): SubscriptionsCommand
    data object ClickSubscribe: SubscriptionsCommand
    data object RefreshData: SubscriptionsCommand
    data class ToggleTopicSelection(val topic: String): SubscriptionsCommand
    data object ClearArticles: SubscriptionsCommand
    data class RemoveSubscription(val topic: String): SubscriptionsCommand
}



data class SubscriptionsState(
    val query: String = "",
    val subscriptions: Map<String, Boolean> = emptyMap(),
    val articles: List<Article> = emptyList<Article>(),
) {
    val subscribeButtonEnabled: Boolean
        get() = query.isNotBlank()
    val selectedTopics: List<String>
        get() = subscriptions.filter { it.value }.map { it.key }
}