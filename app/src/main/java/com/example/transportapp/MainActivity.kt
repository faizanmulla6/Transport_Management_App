// MainActivity.kt
package com.example.transportapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme {
                TransportApp()
            }
        }
    }
}

// Data Models
data class Vehicle(val name: String, val number: String)
data class RO(val name: String, val location: String, val lat: Double, val lng: Double,
              val officerName: String, val officerMobile: String,
              val dealerName: String, val dealerMobile: String)
data class Complaint(
    val id: String = UUID.randomUUID().toString(),
    val vehicle: String = "",
    val ro: String = "",
    val issue: String = "",
    val issueDetails: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
)
data class Feedback(
    val id: String = UUID.randomUUID().toString(),
    val rating: Int = 0,
    val officerFeedback: String = "",
    val managerFeedback: String = "",
    val suggestions: String = "",
    val isAnonymous: Boolean = false,
    val userName: String = "",
    val userMobile: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
)

// Sample Data
object DataProvider {
    val vehicles = listOf(
        Vehicle("Truck TN01AB1234", "TN01AB1234"),
        Vehicle("Tanker MH02CD5678", "MH02CD5678"),
        Vehicle("Lorry KA03EF9012", "KA03EF9012"),
        Vehicle("Container DL04GH3456", "DL04GH3456")
    )

    val ros = listOf(
        RO("Mumbai Central RO", "Mumbai", 19.0176, 72.8562,
            "Raj Kumar", "+91-9876543210", "ABC Dealers", "+91-9876543211"),
        RO("Pune East RO", "Pune", 18.5204, 73.8567,
            "Priya Sharma", "+91-9876543220", "XYZ Motors", "+91-9876543221"),
        RO("Nagpur North RO", "Nagpur", 21.1458, 79.0882,
            "Amit Patil", "+91-9876543230", "DEF Trading", "+91-9876543231")
    )

    val issues = listOf(
        "Vehicle breakdown",
        "Fuel leakage",
        "Tire puncture",
        "Engine problem",
        "Electrical fault",
        "Brake failure",
        "Document missing",
        "Other"
    )
}

// Localization
object Strings {
    val translations = mapOf(
        "en" to mapOf(
            "select_language" to "Select Language",
            "english" to "English",
            "hindi" to "हिंदी",
            "marathi" to "मराठी",
            "services" to "Services",
            "file_complaint" to "File Complaint",
            "ro_details" to "RO Details",
            "trem_card" to "TREM Card",
            "feedback" to "Feedback",
            "officer_login" to "Officer Login",
            "submit" to "Submit",
            "search" to "Search",
            "select_vehicle" to "Select Vehicle",
            "select_ro" to "Select RO",
            "select_issue" to "Select Issue"
        ),
        "hi" to mapOf(
            "select_language" to "भाषा चुनें",
            "english" to "English",
            "hindi" to "हिंदी",
            "marathi" to "मराठी",
            "services" to "सेवाएं",
            "file_complaint" to "शिकायत दर्ज करें",
            "ro_details" to "आरओ विवरण",
            "trem_card" to "ट्रेम कार्ड",
            "feedback" to "प्रतिक्रिया",
            "officer_login" to "अधिकारी लॉगिन",
            "submit" to "जमा करें",
            "search" to "खोजें",
            "select_vehicle" to "वाहन चुनें",
            "select_ro" to "आरओ चुनें",
            "select_issue" to "समस्या चुनें"
        ),
        "mr" to mapOf(
            "select_language" to "भाषा निवडा",
            "english" to "English",
            "hindi" to "हिंदी",
            "marathi" to "मराठी",
            "services" to "सेवा",
            "file_complaint" to "तक्रार नोंदवा",
            "ro_details" to "आरओ तपशील",
            "trem_card" to "ट्रेम कार्ड",
            "feedback" to "अभिप्राय",
            "officer_login" to "अधिकारी लॉगिन",
            "submit" to "सबमिट करा",
            "search" to "शोधा",
            "select_vehicle" to "वाहन निवडा",
            "select_ro" to "आरओ निवडा",
            "select_issue" to "समस्या निवडा"
        )
    )

    fun get(key: String, lang: String): String {
        return translations[lang]?.get(key) ?: key
    }
}

@Composable
fun TransportApp() {
    val navController = rememberNavController()
    var selectedLanguage by remember { mutableStateOf("en") }

    NavHost(navController = navController, startDestination = "language") {
        composable("language") {
            LanguageSelectionScreen(navController) { lang ->
                selectedLanguage = lang
            }
        }
        composable("services") {
            ServiceSelectionScreen(navController, selectedLanguage)
        }
        composable("complaint") {
            ComplaintFormScreen(navController, selectedLanguage)
        }
        composable("ro_details") {
            RODetailsScreen(navController, selectedLanguage)
        }
        composable("trem_card") {
            TREMCardScreen(navController, selectedLanguage)
        }
        composable("feedback") {
            FeedbackScreen(navController, selectedLanguage)
        }
        composable("admin") {
            AdminDashboardScreen(navController, selectedLanguage)
        }
        composable("detail_history/{filter}/{value}") { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter") ?: ""
            val value = backStackEntry.arguments?.getString("value") ?: ""
            DetailHistoryScreen(navController, filter, value, selectedLanguage)
        }
    }
}

// Page 1: Language Selection
@Composable
fun LanguageSelectionScreen(navController: NavHostController, onLanguageSelected: (String) -> Unit) {
    val languages = listOf(
        Triple("en", "English", Color(0xFF4CAF50)),
        Triple("hi", "हिंदी", Color(0xFFFF9800)),
        Triple("mr", "मराठी", Color(0xFF2196F3))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Language / भाषा चुनें / भाषा निवडा",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            languages.forEach { (code, name, color) ->
                var scale by remember { mutableStateOf(1f) }
                val animatedScale by animateFloatAsState(
                    targetValue = scale,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )

                Button(
                    onClick = {
                        onLanguageSelected(code)
                        navController.navigate("services")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(vertical = 8.dp)
                        .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale),
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Page 2: Service Selection
@Composable
fun ServiceSelectionScreen(navController: NavHostController, lang: String) {
    val services = listOf(
        Triple(Strings.get("file_complaint", lang), Icons.Default.Report, Color(0xFFE91E63)) to "complaint",
        Triple(Strings.get("ro_details", lang), Icons.Default.LocationOn, Color(0xFF9C27B0)) to "ro_details",
        Triple(Strings.get("trem_card", lang), Icons.Default.CreditCard, Color(0xFF3F51B5)) to "trem_card",
        Triple(Strings.get("feedback", lang), Icons.Default.Feedback, Color(0xFF009688)) to "feedback"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.get("services", lang),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Service Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(services.chunked(2)) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { (service, route) ->
                            ServiceCard(
                                title = service.first,
                                icon = service.second,
                                color = service.third,
                                modifier = Modifier.weight(1f)
                            ) {
                                navController.navigate(route)
                            }
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Officer Login Button
            Button(
                onClick = { navController.navigate("admin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("officer_login", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ServiceCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Page 3: Complaint Form
@Composable
fun ComplaintFormScreen(navController: NavHostController, lang: String) {
    var searchVehicle by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var searchRO by remember { mutableStateOf("") }
    var selectedRO by remember { mutableStateOf<RO?>(null) }
    var selectedIssue by remember { mutableStateOf("") }
    var issueDetails by remember { mutableStateOf("") }
    var showVehicleDropdown by remember { mutableStateOf(false) }
    var showRODropdown by remember { mutableStateOf(false) }
    var showIssueDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredVehicles = DataProvider.vehicles.filter {
        it.name.contains(searchVehicle, ignoreCase = true) ||
                it.number.contains(searchVehicle, ignoreCase = true)
    }

    val filteredROs = DataProvider.ros.filter {
        it.name.contains(searchRO, ignoreCase = true) ||
                it.location.contains(searchRO, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopBar(title = Strings.get("file_complaint", lang)) {
            navController.popBackStack()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Vehicle Search
            Text(
                text = Strings.get("select_vehicle", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = selectedVehicle?.name ?: searchVehicle,
                onValueChange = {
                    searchVehicle = it
                    selectedVehicle = null
                    showVehicleDropdown = true
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Strings.get("search", lang), color = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { showVehicleDropdown = !showVehicleDropdown }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            if (showVehicleDropdown && filteredVehicles.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn {
                        items(filteredVehicles) { vehicle ->
                            Text(
                                text = vehicle.name,
                                color = Color(0xFF212121),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedVehicle = vehicle
                                        showVehicleDropdown = false
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RO Search
            Text(
                text = Strings.get("select_ro", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = selectedRO?.name ?: searchRO,
                onValueChange = {
                    searchRO = it
                    selectedRO = null
                    showRODropdown = true
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Strings.get("search", lang), color = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { showRODropdown = !showRODropdown }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            if (showRODropdown && filteredROs.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn {
                        items(filteredROs) { ro ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRO = ro
                                        showRODropdown = false
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(text = ro.name, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                                Text(text = ro.location, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Issue Dropdown
            Text(
                text = Strings.get("select_issue", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = selectedIssue,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                placeholder = { Text(Strings.get("select_issue", lang), color = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { showIssueDropdown = !showIssueDropdown }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            if (showIssueDropdown) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn {
                        items(DataProvider.issues) { issue ->
                            Text(
                                text = issue,
                                color = Color(0xFF212121),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIssue = issue
                                        showIssueDropdown = false
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Issue Details (Optional)
            Text(
                text = "Issue Details (Optional)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = issueDetails,
                onValueChange = { issueDetails = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Describe the issue in detail...", color = Color.Gray) },
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    if (selectedVehicle != null && selectedRO != null && selectedIssue.isNotEmpty()) {
                        val complaint = Complaint(
                            vehicle = selectedVehicle!!.number,
                            ro = selectedRO!!.name,
                            issue = selectedIssue,
                            issueDetails = issueDetails
                        )

                        // Save to Firebase
                        val db = FirebaseFirestore.getInstance()
                        db.collection("complaints")
                            .document(complaint.id)
                            .set(complaint)

                        // Send to WhatsApp
                        val message = "New Complaint:\nVehicle: ${complaint.vehicle}\nRO: ${complaint.ro}\nIssue: ${complaint.issue}\nDetails: ${complaint.issueDetails}\nTime: ${complaint.date}"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
                        }
                        context.startActivity(intent)

                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedVehicle != null && selectedRO != null && selectedIssue.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("submit", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Page 4: RO Details
@Composable
fun RODetailsScreen(navController: NavHostController, lang: String) {
    var searchRO by remember { mutableStateOf("") }
    var selectedRO by remember { mutableStateOf<RO?>(null) }
    var showDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredROs = DataProvider.ros.filter {
        it.name.contains(searchRO, ignoreCase = true) ||
                it.location.contains(searchRO, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopBar(title = Strings.get("ro_details", lang)) {
            navController.popBackStack()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = selectedRO?.name ?: searchRO,
                onValueChange = {
                    searchRO = it
                    selectedRO = null
                    showDropdown = true
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Strings.get("search", lang), color = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { showDropdown = !showDropdown }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            if (showDropdown && filteredROs.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn {
                        items(filteredROs) { ro ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRO = ro
                                        showDropdown = false
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(text = ro.name, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                                Text(text = ro.location, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            selectedRO?.let { ro ->
                // Officer Info Card
                InfoCardWithCall(
                    title = "Officer Information",
                    icon = Icons.Default.Person,
                    color = Color(0xFF2196F3),
                    name = ro.officerName,
                    mobile = ro.officerMobile,
                    context = context
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dealer Info Card
                InfoCardWithCall(
                    title = "Dealer Information",
                    icon = Icons.Default.Store,
                    color = Color(0xFF4CAF50),
                    name = ro.dealerName,
                    mobile = ro.dealerMobile,
                    context = context
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Button
                Button(
                    onClick = {
                        val uri = Uri.parse("geo:${ro.lat},${ro.lng}?q=${ro.lat},${ro.lng}(${ro.name})")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Navigate to ${ro.name}", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun InfoCardWithCall(
    title: String,
    icon: ImageVector,
    color: Color,
    name: String,
    mobile: String,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Name", color = Color.Gray, fontSize = 12.sp)
                    Text(text = name, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Mobile", color = Color.Gray, fontSize = 12.sp)
                    Text(text = mobile, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
                }

                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$mobile")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call",
                        tint = color
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, icon: ImageVector, color: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

// Page 5: TREM Card
@Composable
fun TREMCardScreen(navController: NavHostController, lang: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopBar(title = Strings.get("trem_card", lang)) {
            navController.popBackStack()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TREMSection(
                    title = "HAZCHEM Code",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFFF5722),
                    content = {
                        Text(
                            text = "3YE",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Foam or dry agent for fire fighting")
                    }
                )
            }

            item {
                TREMSection(
                    title = "PPE Requirements",
                    icon = Icons.Default.Shield,
                    color = Color(0xFF2196F3),
                    content = {
                        BulletPoint("Full body protective clothing")
                        BulletPoint("Self-contained breathing apparatus")
                        BulletPoint("Chemical resistant gloves")
                        BulletPoint("Safety goggles and face shield")
                    }
                )
            }

            item {
                TREMSection(
                    title = "Fire Fighting Instructions",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFFF9800),
                    content = {
                        BulletPoint("Use foam, dry powder, or CO2 extinguisher")
                        BulletPoint("Do NOT use water jet")
                        BulletPoint("Cool containers with water spray")
                        BulletPoint("Evacuate area if fire gets out of control")
                    }
                )
            }

            item {
                TREMSection(
                    title = "Emergency Contacts",
                    icon = Icons.Default.Phone,
                    color = Color(0xFF4CAF50),
                    content = {
                        EmergencyContact("Fire Department", "101")
                        EmergencyContact("Ambulance", "102")
                        EmergencyContact("Police", "100")
                        EmergencyContact("Depot Officer", "+91-9876543240")
                        EmergencyContact("Regional Manager", "+91-9876543250")
                    }
                )
            }
        }
    }
}

@Composable
fun TREMSection(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("• ", fontSize = 18.sp)
        Text(text = text, fontSize = 16.sp)
    }
}

@Composable
fun EmergencyContact(name: String, number: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(number, fontSize = 14.sp)
        }
    }
}

// Page 6: Feedback
@Composable
fun FeedbackScreen(navController: NavHostController, lang: String) {
    var rating by remember { mutableStateOf(0) }
    var officerFeedback by remember { mutableStateOf("") }
    var managerFeedback by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var userMobile by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopBar(title = Strings.get("feedback", lang)) {
            navController.popBackStack()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rate Your Experience",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (index < rating) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anonymous Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Post as Anonymous",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF212121)
                        )
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personal Details (shown only if not anonymous)
            AnimatedVisibility(visible = !isAnonymous) {
                Column {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your Name") },
                        placeholder = { Text("Enter your name", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userMobile,
                        onValueChange = { userMobile = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mobile Number") },
                        placeholder = { Text("Enter your mobile number", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            OutlinedTextField(
                value = officerFeedback,
                onValueChange = { officerFeedback = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("Officer Feedback", color = Color(0xFF212121)) },
                placeholder = { Text("Share your experience with the officers...", color = Color.Gray) },
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = managerFeedback,
                onValueChange = { managerFeedback = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("Depot Manager Feedback", color = Color(0xFF212121)) },
                placeholder = { Text("Share your experience with the depot manager...", color = Color.Gray) },
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = suggestions,
                onValueChange = { suggestions = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("Suggestions", color = Color(0xFF212121)) },
                placeholder = { Text("Any suggestions for improvement...", color = Color.Gray) },
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF212121),
                    unfocusedTextColor = Color(0xFF212121)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (rating > 0 && (isAnonymous || (userName.isNotEmpty() && userMobile.isNotEmpty()))) {
                        val feedback = Feedback(
                            rating = rating,
                            officerFeedback = officerFeedback,
                            managerFeedback = managerFeedback,
                            suggestions = suggestions,
                            isAnonymous = isAnonymous,
                            userName = if (isAnonymous) "Anonymous" else userName,
                            userMobile = if (isAnonymous) "" else userMobile
                        )

                        val db = FirebaseFirestore.getInstance()
                        db.collection("feedback")
                            .document(feedback.id)
                            .set(feedback)

                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = rating > 0 && (isAnonymous || (userName.isNotEmpty() && userMobile.isNotEmpty())),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("submit", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Page 7: Admin Dashboard
@Composable
fun AdminDashboardScreen(navController: NavHostController, lang: String) {
    var password by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var feedbacks by remember { mutableStateOf<List<Feedback>>(emptyList()) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("complaints")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                complaints = snapshot?.toObjects(Complaint::class.java) ?: emptyList()
            }

        db.collection("feedback")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                feedbacks = snapshot?.toObjects(Feedback::class.java) ?: emptyList()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopBar(title = "Admin Dashboard") {
            navController.popBackStack()
        }

        if (!isAuthenticated) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF1A237E)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Enter Admin Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (password == "admin") {
                            isAuthenticated = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                ) {
                    Text("Login", fontSize = 18.sp)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Date-wise", color = Color(0xFF212121)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Vehicle-wise", color = Color(0xFF212121)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Outlet-wise", color = Color(0xFF212121)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Feedback", color = Color(0xFF212121)) }
                    )
                }

                when (selectedTab) {
                    0 -> ComplaintsList(complaints, navController, "date")
                    1 -> GroupedList(complaints.groupBy { it.vehicle }, navController, "vehicle")
                    2 -> GroupedList(complaints.groupBy { it.ro }, navController, "outlet")
                    3 -> FeedbackList(feedbacks)
                }
            }
        }
    }
}

@Composable
fun FeedbackList(feedbacks: List<Feedback>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(feedbacks) { feedback ->
            FeedbackCard(feedback)
        }
    }
}

@Composable
fun FeedbackCard(feedback: Feedback) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with rating and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(feedback.rating) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = feedback.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (feedback.isAnonymous) Color(0xFFE1BEE7) else Color(0xFFBBDEFB),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = if (feedback.isAnonymous) Icons.Default.VisibilityOff else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (feedback.isAnonymous) Color(0xFF9C27B0) else Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = feedback.userName,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    if (!feedback.isAnonymous && feedback.userMobile.isNotEmpty()) {
                        Text(
                            text = feedback.userMobile,
                            fontSize = 12.sp,
                            color = Color(0xFF616161)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feedbacks
            if (feedback.officerFeedback.isNotEmpty()) {
                Text(
                    text = "Officer Feedback:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                Text(
                    text = feedback.officerFeedback,
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (feedback.managerFeedback.isNotEmpty()) {
                Text(
                    text = "Manager Feedback:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = feedback.managerFeedback,
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (feedback.suggestions.isNotEmpty()) {
                Text(
                    text = "Suggestions:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = feedback.suggestions,
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )
            }
        }
    }
}

@Composable
fun ComplaintsList(complaints: List<Complaint>, navController: NavHostController, filter: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(complaints) { complaint ->
            ComplaintCard(complaint) {
                navController.navigate("detail_history/$filter/${complaint.vehicle}")
            }
        }
    }
}

@Composable
fun GroupedList(groupedComplaints: Map<String, List<Complaint>>, navController: NavHostController, filter: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groupedComplaints.keys.toList()) { key ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("detail_history/$filter/$key")
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = key,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${groupedComplaints[key]?.size} complaints",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun ComplaintCard(complaint: Complaint, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = complaint.vehicle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = complaint.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "RO: ${complaint.ro}", fontSize = 14.sp, color = Color(0xFF616161))
            Text(text = "Issue: ${complaint.issue}", fontSize = 14.sp, color = Color(0xFFFF5722))

            if (complaint.issueDetails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Details: ${complaint.issueDetails}",
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    maxLines = 2
                )
            }
        }
    }
}

// Page 8: Detail History
@Composable
fun DetailHistoryScreen(navController: NavHostController, filter: String, value: String, lang: String) {
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val query = when (filter) {
            "vehicle" -> db.collection("complaints").whereEqualTo("vehicle", value)
            "outlet" -> db.collection("complaints").whereEqualTo("ro", value)
            else -> db.collection("complaints")
        }

        query.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                complaints = snapshot?.toObjects(Complaint::class.java) ?: emptyList()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopBar(title = "History: $value") {
            navController.popBackStack()
        }

        if (complaints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No complaints found", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(complaints) { complaint ->
                    ComplaintCard(complaint) {}
                }
            }
        }
    }
}

// Common Components
@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}