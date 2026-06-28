package net.lapieceuniquedijon.djinterface;

/**
 * Modèle représentant une demande de titre.
 */
public class TrackRequest {
    private String timestamp;
    private String date;
    private String time;
    private String ip;
    private String title;
    private String spotifyId;
    private String status;
    private String imageUrl;

    public TrackRequest() {}

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
