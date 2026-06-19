package com.example.anilogue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.anilogue.ui.theme.AniLogueTheme
import kotlinx.coroutines.delay

data class AnimeEpisode(
    val id: Int,
    val judulEpisode: String,
    val imageRes: Int,
    val audioRes: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AniLogueTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("Home") }
    var selectedTab by remember { mutableStateOf(0) }

    // State Koleksi (Bookmark) Dinamis
    var bookmarkedAnimes by remember { mutableStateOf(setOf<String>()) }

    var selectedEpisode by remember { mutableStateOf<AnimeEpisode?>(null) }
    var selectedAnimeTitle by remember { mutableStateOf("Violet Evergarden") }

    // Fitur Riwayat (History)
    var playHistory by remember { mutableStateOf(listOf<AnimeEpisode>()) }

    BackHandler(enabled = currentScreen != "Home" || selectedTab != 0) {
        if (currentScreen == "Player") {
            currentScreen = "Episodes"
        } else if (currentScreen == "Episodes") {
            currentScreen = "Home"
        } else if (currentScreen == "Home" && selectedTab != 0) {
            selectedTab = 0
        }
    }

    val navItems = listOf(
        NavigationItem("Home", Icons.Default.Home),
        NavigationItem("Search", Icons.Default.Search),
        NavigationItem("Library", Icons.Default.LibraryMusic)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF121212),
        bottomBar = {
            if (currentScreen == "Home") {
                NavigationBar(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                if (index == 0) currentScreen = "Home"
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Red,
                                selectedTextColor = Color.Red,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> {
                    when (currentScreen) {
                        "Home" -> HomeScreen(
                            bookmarkedAnimes = bookmarkedAnimes,
                            onToggleLibrary = { title ->
                                bookmarkedAnimes = if (bookmarkedAnimes.contains(title)) {
                                    bookmarkedAnimes - title
                                } else {
                                    bookmarkedAnimes + title
                                }
                            },
                            onAnimeClick = { title ->
                                selectedAnimeTitle = title
                                currentScreen = "Episodes"
                            }
                        )
                        "Episodes" -> EpisodeListScreen(
                            animeTitle = selectedAnimeTitle,
                            onBackClick = { currentScreen = "Home" },
                            onEpisodeClick = { episode ->
                                selectedEpisode = episode
                                // FIX: Filter berdasarkan seluruh objek episode, bukan cuma ID-nya
                                playHistory = (listOf(episode) + playHistory.filter { it != episode }).take(10)
                                currentScreen = "Player"
                            }
                        )
                        "Player" -> {
                            selectedEpisode?.let { episode ->
                                PlayerScreen(
                                    episode = episode,
                                    seriesTitle = selectedAnimeTitle,
                                    onBackClick = { currentScreen = "Episodes" }
                                )
                            }
                        }
                    }
                }
                1 -> SearchScreen(
                    onAnimeClick = { title ->
                        selectedAnimeTitle = title
                        selectedTab = 0
                        currentScreen = "Episodes"
                    }
                )
                2 -> LibraryScreen(
                    bookmarkedAnimes = bookmarkedAnimes,
                    playHistory = playHistory,
                    onAnimeClick = { title ->
                        selectedAnimeTitle = title
                        selectedTab = 0
                        currentScreen = "Episodes"
                    },
                    onEpisodeClick = { episode ->
                        selectedEpisode = episode
                        // FIX: Filter berdasarkan seluruh objek episode, bukan cuma ID-nya
                        playHistory = (listOf(episode) + playHistory.filter { it != episode }).take(10)
                        selectedTab = 0
                        currentScreen = "Player"
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    bookmarkedAnimes: Set<String>,
    onToggleLibrary: (String) -> Unit,
    onAnimeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Anime Playlist",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimeCoverCard(
                    title = "Violet Evergarden",
                    imageRes = R.drawable.cover_violet,
                    isInLibrary = bookmarkedAnimes.contains("Violet Evergarden"),
                    onToggleLibrary = { onToggleLibrary("Violet Evergarden") },
                    onClick = { onAnimeClick("Violet Evergarden") },
                    showLibraryButton = true
                )
            }
            item {
                AnimeCoverCard(
                    title = "Chou Kaguya-Hime!",
                    imageRes = R.drawable.coverkaguya,
                    isInLibrary = bookmarkedAnimes.contains("Chou Kaguya-Hime!"),
                    onToggleLibrary = { onToggleLibrary("Chou Kaguya-Hime!") },
                    onClick = { onAnimeClick("Chou Kaguya-Hime!") },
                    showLibraryButton = true
                )
            }
            item {
                AnimeCoverCard(
                    title = "Koe no Katachi",
                    imageRes = R.drawable.coverkoe,
                    isInLibrary = bookmarkedAnimes.contains("Koe no Katachi"),
                    onToggleLibrary = { onToggleLibrary("Koe no Katachi") },
                    onClick = { onAnimeClick("Koe no Katachi") },
                    showLibraryButton = true
                )
            }
        }
    }
}

@Composable
fun AnimeCoverCard(
    title: String,
    imageRes: Int,
    isInLibrary: Boolean,
    onToggleLibrary: () -> Unit,
    onClick: () -> Unit,
    showLibraryButton: Boolean = true
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            if (showLibraryButton) {
                IconButton(
                    onClick = onToggleLibrary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (isInLibrary) Icons.Default.Bookmark else Icons.Default.Add,
                        contentDescription = "Add to Library",
                        tint = if (isInLibrary) Color.Red else Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun SearchScreen(onAnimeClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val allAnimes = listOf(
        "Violet Evergarden" to R.drawable.cover_violet,
        "Chou Kaguya-Hime!" to R.drawable.coverkaguya,
        "Koe no Katachi" to R.drawable.coverkoe
    )

    val filteredAnimes = if (searchQuery.isBlank()) {
        allAnimes
    } else {
        allAnimes.filter {
            it.first.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFF121212))
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ketik 'koe', 'kaguya', atau 'violet'...", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.Red,
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (filteredAnimes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Anime tidak ditemukan.\nCoba kata kunci lain.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                gridItems(filteredAnimes) { (title, imageRes) ->
                    AnimeCoverCard(
                        title = title,
                        imageRes = imageRes,
                        isInLibrary = false,
                        onToggleLibrary = {},
                        onClick = { onAnimeClick(title) },
                        showLibraryButton = false
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    bookmarkedAnimes: Set<String>,
    playHistory: List<AnimeEpisode>,
    onAnimeClick: (String) -> Unit,
    onEpisodeClick: (AnimeEpisode) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFF121212))
    ) {
        item {
            Text(
                text = "Koleksi Saya",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (bookmarkedAnimes.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bookmarkedAnimes.toList()) { title ->
                        val imageRes = when (title) {
                            "Chou Kaguya-Hime!" -> R.drawable.coverkaguya
                            "Koe no Katachi" -> R.drawable.coverkoe
                            else -> R.drawable.cover_violet
                        }
                        Box(modifier = Modifier.width(140.dp)) {
                            AnimeCoverCard(
                                title = title,
                                imageRes = imageRes,
                                isInLibrary = true,
                                onToggleLibrary = {},
                                onClick = { onAnimeClick(title) },
                                showLibraryButton = false
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Koleksi kosong. Tambahkan anime dari Home",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Riwayat Terakhir",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (playHistory.isEmpty()) {
            item {
                Text(text = "Belum ada riwayat putar.", color = Color.Gray)
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(playHistory) { episode ->
                        HistoryEpisodeCard(episode = episode, onClick = { onEpisodeClick(episode) })
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryEpisodeCard(episode: AnimeEpisode, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = episode.imageRes),
            contentDescription = episode.judulEpisode,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = episode.judulEpisode,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EpisodeListScreen(animeTitle: String, onBackClick: () -> Unit, onEpisodeClick: (AnimeEpisode) -> Unit) {
    var isExpanded by remember(animeTitle) { mutableStateOf(false) }

    val episodes = remember(animeTitle) {
        when (animeTitle) {
            "Chou Kaguya-Hime!" -> listOf(
                AnimeEpisode(1, "Movie", R.drawable.kaguya, R.raw.kaguya)
            )
            "Koe no Katachi" -> listOf(
                AnimeEpisode(1, "Movie", R.drawable.koe, R.raw.koe)
            )
            else -> listOf(
                AnimeEpisode(1, "Episode 1", R.drawable.eps_01, R.raw.eps_01),
                AnimeEpisode(2, "Episode 2", R.drawable.eps_02, R.raw.eps_02),
                AnimeEpisode(3, "Episode 3", R.drawable.eps_03, R.raw.eps_03),
                AnimeEpisode(4, "Episode 4", R.drawable.eps_04, R.raw.eps_04),
                AnimeEpisode(5, "Episode 5", R.drawable.eps_05, R.raw.eps_05),
                AnimeEpisode(6, "Episode 6", R.drawable.eps_06, R.raw.eps_06),
                AnimeEpisode(7, "Episode 7", R.drawable.eps_07, R.raw.eps_07),
                AnimeEpisode(8, "Episode 8", R.drawable.eps_08, R.raw.eps_08),
                AnimeEpisode(9, "Episode 9", R.drawable.eps_09, R.raw.eps_09),
                AnimeEpisode(10, "Episode 10", R.drawable.eps_10, R.raw.eps_10),
                AnimeEpisode(11, "Episode 11", R.drawable.eps_11, R.raw.eps_11),
                AnimeEpisode(12, "Episode 12", R.drawable.eps_12, R.raw.eps_12),
                AnimeEpisode(13, "Episode 13", R.drawable.eps_13, R.raw.eps_13)
            )
        }
    }

    val displayTitle = animeTitle // Menampilkan judul aslinya

    val synopsis = when (animeTitle) {
        "Chou Kaguya-Hime!" -> "Tsukuyomi adalah dunia virtual tempat harapan dan impian berkumpul. Iroha Sakayori, seorang siswi SMA berusia 17 tahun di Tokyo, menjalani kehidupan yang sangat sibuk mencoba menyeimbangkan kerja paruh waktu dan akademis. Ia menemukan kedamaian dengan menonton seorang streamer populer bernama Yachiyo Runami, yang merupakan administrator ruang virtual online bernama Tsukuyomi. Iroha sering mengunjungi Tsukuyomi dan menghabiskan waktu dengan mendukung Yachiyo dan memainkan game pertarungan untuk mendapatkan sedikit uang saku. Suatu hari, Iroha menemukan sebuah tiang listrik bersinar. Yang mengejutkannya, seorang bayi muncul dari tiang tersebut. Iroha membawa bayi itu pulang dan menyaksikannya tumbuh dengan cepat menjadi seorang gadis seusianya.\n\n\"Apakah kamu Putri Kaguya?\"\n\nKaguya yang telah dewasa mengembangkan kepribadian yang manja. Atas permintaannya, Iroha membantunya menjadi streamer di Tsukuyomi. Tanpa mereka sadari, kekuatan gelap sedang mengintai, ingin membawa Kaguya kembali ke bulan."
        "Koe no Katachi" -> "Sebagai anak muda yang liar, siswa SD Shouya Ishida berusaha menghilangkan kebosanan dengan cara yang paling kejam. Ketika Shouko Nishimiya yang tunarungu pindah ke kelasnya, Shouya dan seluruh teman sekelasnya tanpa berpikir panjang merundungnya demi kesenangan. Namun, ketika ibunya melapor ke pihak sekolah, Shouya dijadikan sasaran dan disalahkan atas semua yang terjadi pada Shouko. Setelah Shouko pindah sekolah, Shouya ditinggalkan di bawah belas kasihan teman-teman sekelasnya. Ia dikucilkan tanpa ampun sepanjang masa SD dan SMP, sementara para guru menutup mata.\n\nKini di tahun ketiga SMA-nya, Shouya masih dihantui oleh kesalahan yang ia perbuat sewaktu kecil. Dengan tulus menyesali perbuatan masa lalunya, ia memulai perjalanan penebusan dosa: untuk bertemu Shouko sekali lagi dan memperbaiki segalanya.\n\nKoe no Katachi menceritakan kisah yang mengharukan tentang reuni Shouya dengan Shouko dan usaha jujurnya untuk menebus kesalahan, sembari terus dihantui oleh bayang-bayang masa lalunya."
        else -> "Perang Besar akhirnya berakhir setelah empat tahun konflik yang panjang; terbelah menjadi dua, benua Telesis perlahan mulai bangkit dan berkembang kembali. Terjebak dalam pertumpahan darah tersebut adalah Violet Evergarden, seorang gadis muda yang dibesarkan hanya dengan satu tujuan: menghancurkan garis pertahanan musuh. Dirawat di rumah sakit dan cacat akibat pertempuran berdarah di akhir perang, ia hanya ditinggali kata-kata dari orang yang paling disayanginya, namun ia sama sekali tidak memahami makna kata-kata tersebut.\n\nDalam masa pemulihan lukanya, Violet memulai kehidupan baru dengan bekerja di CH Postal Services. Di sana, secara kebetulan ia menyaksikan pekerjaan Auto Memory Doll (Boneka Penulis Otomatis), yaitu seseorang yang bertugas menuangkan pikiran dan perasaan orang lain ke dalam kata-kata di atas kertas. Tergerak oleh hal itu, Violet mulai bekerja sebagai Auto Memory Doll, sebuah profesi yang akan membawanya pada penemuan jati diri dan mengubah hidup klien-kliennya."
    }

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = displayTitle,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = synopsis,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Justify,
                        lineHeight = 20.sp,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isExpanded) "Sembunyikan ▲" else "Baca Selengkapnya ▼",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }
            }
            items(episodes) { episode ->
                EpisodeItem(episode, onClick = { onEpisodeClick(episode) })
            }
        }
    }
}

@Composable
fun EpisodeItem(episode: AnimeEpisode, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = episode.imageRes),
                contentDescription = episode.judulEpisode,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = episode.judulEpisode,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(episode: AnimeEpisode, seriesTitle: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = RawResourceDataSource.buildRawResourceUri(episode.audioRes)
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(500L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        Image(
            painter = painterResource(id = episode.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            if (offset.x < size.width / 2) {
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000L).coerceAtLeast(0L))
                            } else {
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration))
                            }
                        }
                    )
                },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = episode.judulEpisode,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = seriesTitle,
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { newValue ->
                if (duration > 0) {
                    val seekTo = (newValue * duration).toLong()
                    exoPlayer.seekTo(seekTo)
                }
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentPosition), color = Color.Gray, fontSize = 12.sp)
            Text(text = formatTime(duration), color = Color.Gray, fontSize = 12.sp)
        }

        TextButton(onClick = {
            val currentIndex = speeds.indexOf(playbackSpeed)
            val nextIndex = (currentIndex + 1) % speeds.size
            playbackSpeed = speeds[nextIndex]
            exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
        }) {
            Text(text = "Speed: ${playbackSpeed}x", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 2000L).coerceAtLeast(0L))
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.FastRewind,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "-2s",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Surface(
                onClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        isPlaying = false
                    } else {
                        exoPlayer.play()
                        isPlaying = true
                    }
                },
                shape = RoundedCornerShape(50),
                color = Color.Red,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    exoPlayer.seekTo((exoPlayer.currentPosition + 2000L).coerceAtMost(exoPlayer.duration))
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "+2s",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

data class NavigationItem(val title: String, val icon: ImageVector)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AniLogueTheme {
        MainScreen()
    }
}