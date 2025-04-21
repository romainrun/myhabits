package com.rrtech.myhabits.data.db;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.rrtech.myhabits.data.model.Habit;
import com.rrtech.myhabits.data.model.HabitCheck;
import com.rrtech.myhabits.data.model.Routine;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                Habit.class,
                HabitCheck.class,
                Routine.class,
                HabitRoutineCrossRef.class
        },
        version = 31,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract HabitDao habitDao();
    public abstract HabitCheckDao habitCheckDao();
    public abstract RoutinesDao routinesDao(); // ✅ anciennement habitGroupDao
    public abstract HabitRoutineDao habitRoutineDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "myhabits.db")
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    databaseWriteExecutor.execute(() -> {
                                        if (isDatabaseEmpty(getInstance(context))) {
                                            prepopulateDatabase(getInstance(context));
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static boolean isDatabaseEmpty(AppDatabase db) {
        List<Habit> all = db.habitDao().getAllHabitsSync();
        return all == null || all.isEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void prepopulateDatabase(AppDatabase db) {
        HabitDao habitDao = db.habitDao();
        HabitCheckDao checkDao = db.habitCheckDao();
        HabitRoutineDao routineDao = db.habitRoutineDao();

        // 🔄 Jours formatés sous forme d'entiers
        String today = getDayAsIntegerString(LocalDate.now());               // ex: "2"
        String tomorrow = getDayAsIntegerString(LocalDate.now().plusDays(1));
        String yesterday = getDayAsIntegerString(LocalDate.now().minusDays(1));

        // 📌 Habitudes
        Habit habit0 = new Habit("Aller à la salle", "💪", "#4CAF50", today, 1);
        Habit habit1 = new Habit("Lire un livre", "📚", "#2196F3", today, 2);
        Habit habitTomorrow = new Habit("Préparer les repas", "🥗", "#FF9800", tomorrow, 1);
        Habit habitMissed = new Habit("Faire du ménage", "🧹", "#E91E63", yesterday, 1);

        int id0 = (int) habitDao.insert(habit0);
        int id1 = (int) habitDao.insert(habit1);
        int id2 = (int) habitDao.insert(habitTomorrow);
        int id3 = (int) habitDao.insert(habitMissed);

        // ✅ Check simulé aujourd’hui
        checkDao.insert(new HabitCheck(LocalDate.now().toString(), id0));

        // ➕ Routines préremplies
        Routine routine1 = new Routine("Routine Matin", true);
        Routine routine2 = new Routine("Routine Soir", true);

        int routineId1 = (int) routineDao.insert(routine1);
        int routineId2 = (int) routineDao.insert(routine2);

        // 🔗 Lier les habitudes à des routines
        db.habitRoutineDao().insert(new HabitRoutineCrossRef(id0, routineId1));
        db.habitRoutineDao().insert(new HabitRoutineCrossRef(id1, routineId1));
        db.habitRoutineDao().insert(new HabitRoutineCrossRef(id2, routineId2));
        db.habitRoutineDao().insert(new HabitRoutineCrossRef(id3, routineId2));
    }


   @RequiresApi(api = Build.VERSION_CODES.O)
    private static String getDayAsIntegerString(LocalDate date) {
        return String.valueOf(date.getDayOfWeek().getValue()); // 1 = Lundi, ..., 7 = Dimanche
    }
}
