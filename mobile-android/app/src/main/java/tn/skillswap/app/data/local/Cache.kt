package tn.skillswap.app.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tn.skillswap.app.data.CategorieInfo
import tn.skillswap.app.data.Competence
import tn.skillswap.app.data.Exchange

// ------------------------------------------------------------------ Entités Room
@Entity(tableName = "competences")
data class CompetenceEntity(
    @PrimaryKey val id: Long,
    val titre: String,
    val description: String,
    val niveau: String,
    val disponibilites: String?,
    val statutValidation: String,
    val categorieId: Long?,
    val categorieNom: String?,
    val etudiantId: Long?,
    val documentUrl: String?,
    val documentName: String?,
    val documentSize: Long?,
)

@Entity(tableName = "echanges")
data class ExchangeEntity(
    @PrimaryKey val id: Long,
    /** "SENT" (demandes que j'ai envoyées) ou "RECEIVED" (demandes reçues). */
    val origine: String,
    val requesterId: Long,
    val providerId: Long,
    val skillId: Long,
    val skillTitle: String,
    val description: String?,
    val durationHours: Int,
    val proposedDateTime: String,
    val status: String,
    val meetingLink: String?,
)

// ------------------------------------------------------------------ DAO
@Dao
interface CacheDao {
    @Query("SELECT * FROM competences ORDER BY id DESC")
    fun competences(): Flow<List<CompetenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insererCompetences(liste: List<CompetenceEntity>)

    @Query("DELETE FROM competences WHERE id NOT IN (:ids)")
    suspend fun supprimerCompetencesAbsentes(ids: List<Long>)

    @Query("SELECT * FROM echanges WHERE origine = :origine ORDER BY id DESC")
    fun echanges(origine: String): Flow<List<ExchangeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insererEchanges(liste: List<ExchangeEntity>)

    @Query("DELETE FROM echanges WHERE origine = :origine AND id NOT IN (:ids)")
    suspend fun supprimerEchangesAbsents(origine: String, ids: List<Long>)
}

@Database(entities = [CompetenceEntity::class, ExchangeEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao

    companion object {
        fun creer(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "skillswap.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}

// ------------------------------------------------------------------ Façade testable
/**
 * Cache local des données lues sur le serveur : l'application reste utilisable hors ligne.
 * Les ViewModels dépendent de cette interface (et non de Room) pour pouvoir être testés.
 */
interface CacheStore {
    fun competences(): Flow<List<Competence>>
    suspend fun remplacerCompetences(liste: List<Competence>)
    fun echanges(origine: String): Flow<List<Exchange>>
    suspend fun remplacerEchanges(origine: String, liste: List<Exchange>)
}

class RoomCacheStore(private val dao: CacheDao) : CacheStore {
    override fun competences(): Flow<List<Competence>> = dao.competences().map { l -> l.map { it.versModele() } }

    override suspend fun remplacerCompetences(liste: List<Competence>) {
        dao.insererCompetences(liste.map { it.versEntite() })
        dao.supprimerCompetencesAbsentes(liste.map { it.id })
    }

    override fun echanges(origine: String): Flow<List<Exchange>> = dao.echanges(origine).map { l -> l.map { it.versModele() } }

    override suspend fun remplacerEchanges(origine: String, liste: List<Exchange>) {
        dao.insererEchanges(liste.map { it.versEntite(origine) })
        dao.supprimerEchangesAbsents(origine, liste.map { it.id })
    }
}

fun Competence.versEntite() = CompetenceEntity(
    id, titre, description, niveau, disponibilites, statutValidation,
    categorie?.id, categorie?.nom, etudiantId, documentUrl, documentName, documentSize,
)

fun CompetenceEntity.versModele() = Competence(
    id = id, titre = titre, description = description, niveau = niveau, disponibilites = disponibilites,
    statutValidation = statutValidation,
    categorie = categorieNom?.let { CategorieInfo(categorieId ?: 0, it) },
    etudiantId = etudiantId, documentUrl = documentUrl, documentName = documentName, documentSize = documentSize,
)

fun Exchange.versEntite(origine: String) = ExchangeEntity(
    id, origine, requesterId, providerId, skillId, skillTitle, description,
    durationHours, proposedDateTime, status, meetingLink,
)

fun ExchangeEntity.versModele() = Exchange(
    id = id, requesterId = requesterId, providerId = providerId, skillId = skillId, skillTitle = skillTitle,
    description = description, durationHours = durationHours, proposedDateTime = proposedDateTime,
    status = status, meetingLink = meetingLink,
)
