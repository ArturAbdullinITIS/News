package com.example.news.presentation.screen.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.news.R


@Composable
fun SubscriptionsScreen(
    onNavigateToSettings: () -> Unit
) {
    SubscriptionsContent(
        onNavigateToSettings = onNavigateToSettings
    )
}



@Composable
fun SubscriptionsContent(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit,
    viewModel: SubscriptionsViewModel = hiltViewModel()

) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SubscriptionsTopBar(
                onRefreshDataClick = {
                    viewModel.processCommand(SubscriptionsCommand.RefreshData)
                },
                onClearArticlesClick = {
                    viewModel.processCommand(SubscriptionsCommand.ClearArticles)
                },
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item() {
                Subscriptions(
                    subscriptions = state.subscriptions,
                    query = state.query,
                    isSubButtonEnabled = state.subscribeButtonEnabled,
                    onQueryChanged = {
                        viewModel.processCommand(SubscriptionsCommand.InputTopic(it))
                    },
                    onDeleteSub = {
                        viewModel.processCommand(SubscriptionsCommand.RemoveSubscription(it))
                    },
                    onTopicClick = {
                        viewModel.processCommand(SubscriptionsCommand.ToggleTopicSelection(it))
                    },
                    onSubscribeClick = {
                        viewModel.processCommand(SubscriptionsCommand.ClickSubscribe)
                    }
                )
            }
            if (state.articles.isNotEmpty()) {
                item {
                    HorizontalDivider()
                }
                item {
                    Text(
                        text = stringResource(R.string.articles, state.articles.size),
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    HorizontalDivider()
                }
                items(
                    items = state.articles,
                    key = { it.url }
                ) {
                    ArticleCard(article = it)
                }
            } else if(state.subscriptions.isNotEmpty()){
                item {
                    HorizontalDivider()
                }
                item {
                    Text(
                        text = stringResource(R.string.no_articles_for_selected_subscriptions),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

    }

}