package com.pizzadelivery.android.ui.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pizzadelivery.android.data.model.Order
import com.pizzadelivery.android.data.model.OrderStatus
import com.pizzadelivery.android.util.toCurrency
import com.pizzadelivery.android.util.toFormattedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    onNavigateToOrder: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = ordersState) {
            is OrdersUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is OrdersUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadOrders() }) { Text("Retry") }
                    }
                }
            }
            is OrdersUiState.Success -> {
                if (state.orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No orders yet", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.orders) { order ->
                            OrderCard(order = order, onClick = { onNavigateToOrder(order.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.storeName.ifEmpty { "Order #${order.id.take(8)}" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OrderStatusChip(status = order.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${order.items.size} items • ${order.total.toCurrency()}", style = MaterialTheme.typography.bodyMedium)
            Text(order.createdAt.toFormattedDate(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (color, text) = when (status) {
        OrderStatus.PLACED -> MaterialTheme.colorScheme.secondary to "Placed"
        OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.primary to "Confirmed"
        OrderStatus.PREPARING -> MaterialTheme.colorScheme.secondary to "Preparing"
        OrderStatus.READY -> MaterialTheme.colorScheme.tertiary to "Ready"
        OrderStatus.PICKED_UP -> MaterialTheme.colorScheme.primary to "Picked Up"
        OrderStatus.ON_THE_WAY -> MaterialTheme.colorScheme.primary to "On the Way"
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiary to "Delivered"
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
    }
    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color)
    )
}
