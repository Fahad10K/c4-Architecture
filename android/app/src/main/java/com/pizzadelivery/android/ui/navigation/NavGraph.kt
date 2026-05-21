package com.pizzadelivery.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pizzadelivery.android.ui.auth.LoginScreen
import com.pizzadelivery.android.ui.auth.RegisterScreen
import com.pizzadelivery.android.ui.auth.AuthViewModel
import com.pizzadelivery.android.ui.home.HomeScreen
import com.pizzadelivery.android.ui.store.StoreListScreen
import com.pizzadelivery.android.ui.store.StoreDetailScreen
import com.pizzadelivery.android.ui.menu.MenuScreen
import com.pizzadelivery.android.ui.menu.MenuItemDetailScreen
import com.pizzadelivery.android.ui.cart.CartScreen
import com.pizzadelivery.android.ui.checkout.CheckoutScreen
import com.pizzadelivery.android.ui.order.OrderListScreen
import com.pizzadelivery.android.ui.order.OrderDetailScreen
import com.pizzadelivery.android.ui.tracking.TrackingScreen
import com.pizzadelivery.android.ui.notification.NotificationScreen
import com.pizzadelivery.android.ui.chatbot.ChatbotScreen
import com.pizzadelivery.android.ui.profile.ProfileScreen
import com.pizzadelivery.android.ui.profile.AddressScreen
import com.pizzadelivery.android.ui.search.SearchScreen
import com.pizzadelivery.android.ui.admin.AdminDashboardScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Stores : Screen("stores")
    object StoreDetail : Screen("store/{storeId}") {
        fun createRoute(storeId: String) = "store/$storeId"
    }
    object Menu : Screen("menu/{storeId}") {
        fun createRoute(storeId: String) = "menu/$storeId"
    }
    object MenuItemDetail : Screen("menu-item/{itemId}") {
        fun createRoute(itemId: String) = "menu-item/$itemId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order/{orderId}") {
        fun createRoute(orderId: String) = "order/$orderId"
    }
    object Tracking : Screen("tracking/{orderId}") {
        fun createRoute(orderId: String) = "tracking/$orderId"
    }
    object Notifications : Screen("notifications")
    object Chatbot : Screen("chatbot")
    object Profile : Screen("profile")
    object Addresses : Screen("addresses")
    object Search : Screen("search")
    object Admin : Screen("admin")
}

@Composable
fun PizzaDeliveryNavHost(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToStore = { storeId ->
                    navController.navigate(Screen.StoreDetail.createRoute(storeId))
                },
                onNavigateToStores = { navController.navigate(Screen.Stores.route) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToChatbot = { navController.navigate(Screen.Chatbot.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
            )
        }

        composable(Screen.Stores.route) {
            StoreListScreen(
                onNavigateToStore = { storeId ->
                    navController.navigate(Screen.StoreDetail.createRoute(storeId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) {
            StoreDetailScreen(
                onNavigateToMenu = { storeId ->
                    navController.navigate(Screen.Menu.createRoute(storeId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) {
            MenuScreen(
                onNavigateToItem = { itemId ->
                    navController.navigate(Screen.MenuItemDetail.createRoute(itemId))
                },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MenuItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            MenuItemDetailScreen(
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) },
                onNavigateToMenu = { storeId ->
                    navController.navigate(Screen.Menu.createRoute(storeId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.Tracking.createRoute(orderId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Orders.route) {
            OrderListScreen(
                onNavigateToOrder = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            OrderDetailScreen(
                onNavigateToTracking = { orderId ->
                    navController.navigate(Screen.Tracking.createRoute(orderId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Tracking.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            TrackingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chatbot.route) {
            ChatbotScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Addresses.route) {
            AddressScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToItem = { itemId ->
                    navController.navigate(Screen.MenuItemDetail.createRoute(itemId))
                },
                onNavigateToStore = { storeId ->
                    navController.navigate(Screen.StoreDetail.createRoute(storeId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Admin.route) {
            AdminDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
