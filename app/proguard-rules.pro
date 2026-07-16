# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep class com.pontifex.app.data.db.entity.** { *; }

# Compose
-dontwarn androidx.compose.**

# Termux Terminal Emulator
-keep class com.termux.terminal.** { *; }
-keep class com.termux.terminal.TerminalSession { *; }
-keep class com.termux.terminal.TerminalEmulator { *; }
-keep class com.termux.terminal.TerminalBuffer { *; }
-keep class com.termux.terminal.TerminalRow { *; }
-keep class com.termux.terminal.TerminalSessionClient { *; }
-keep class com.termux.terminal.TerminalOutput { *; }
-keep class com.termux.terminal.TextStyle { *; }
-keep class com.termux.terminal.KeyHandler { *; }
-keep class com.termux.terminal.WcWidth { *; }
-keep class com.termux.terminal.JNI { *; }
