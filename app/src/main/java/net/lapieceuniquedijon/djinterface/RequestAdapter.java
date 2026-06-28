package net.lapieceuniquedijon.djinterface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * Adaptateur RecyclerView pour la liste des demandes de titres.
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface OnActionListener {
        void onPlay(TrackRequest request, int position);
        void onSkip(TrackRequest request, int position);
    }

    private final List<TrackRequest> items;
    private final OnActionListener listener;

    public RequestAdapter(List<TrackRequest> items, OnActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        TrackRequest req = items.get(position);

        h.tvTitle.setText(req.getTitle() != null ? req.getTitle() : "—");
        h.tvRequester.setText(
                h.itemView.getContext().getString(
                        R.string.requested_by,
                        req.getIp() != null ? req.getIp() : "?",
                        req.getTime() != null ? req.getTime() : "?"
                )
        );

        // Pochette album via Glide
        if (req.getImageUrl() != null && !req.getImageUrl().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(req.getImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                    .placeholder(android.R.color.darker_gray)
                    .into(h.imgAlbum);
        } else {
            h.imgAlbum.setImageResource(android.R.color.darker_gray);
        }

        h.btnPlay.setOnClickListener(v -> {
            h.itemView.setAlpha(0.5f);
            listener.onPlay(req, h.getAdapterPosition());
        });

        h.btnSkip.setOnClickListener(v -> {
            h.itemView.setAlpha(0.5f);
            listener.onSkip(req, h.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void removeAt(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void resetAlphaAt(int position) {
        if (position >= 0 && position < items.size()) {
            notifyItemChanged(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAlbum;
        TextView tvTitle;
        TextView tvRequester;
        Button btnPlay;
        Button btnSkip;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbum    = itemView.findViewById(R.id.imgAlbum);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvRequester = itemView.findViewById(R.id.tvRequester);
            btnPlay     = itemView.findViewById(R.id.btnPlay);
            btnSkip     = itemView.findViewById(R.id.btnSkip);
        }
    }
}
