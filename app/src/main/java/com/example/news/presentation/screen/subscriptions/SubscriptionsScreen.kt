package com.example.news.presentation.screen.subscriptions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@Composable
fun SubscriptionsScreen(
    onNavigateToSettings: () -> Unit
) {

}



@Composable
fun SubscriptionsContent(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit,
    viewModel: SubscriptionsViewModel = hiltViewModel()
) {


}