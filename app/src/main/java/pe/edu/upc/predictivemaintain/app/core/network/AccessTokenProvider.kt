package pe.edu.upc.predictivemaintain.app.core.network

interface AccessTokenProvider {
    fun getAccessToken(): String?
}
