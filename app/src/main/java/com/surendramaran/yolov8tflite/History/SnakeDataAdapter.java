package com.surendramaran.yolov8tflite.History;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.surendramaran.yolov8tflite.R;
import java.util.List;
public class SnakeDataAdapter extends RecyclerView.Adapter<SnakeDataAdapter.SnakeDataViewHolder> {
    private List<SnakeData> snakeDataList;
    public SnakeDataAdapter(List<SnakeData> snakeDataList) {
        this.snakeDataList = snakeDataList;
    }
    @NonNull
    @Override
    public SnakeDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.snake_data_item, parent, false);
        return new SnakeDataViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull SnakeDataViewHolder holder, int position) {
        SnakeData snakeData = snakeDataList.get(position);
        holder.titleTextView.setText(snakeData.getTitle());
        holder.confidenceTextView.setText("Confidence: " + snakeData.getConfidence());
        holder.locationTextView.setText("location:"+snakeData.getLocation());
        Glide.with(holder.itemView.getContext())
                .load(snakeData.getImageUrl())
                .into(holder.imageView);
    }
    @Override
    public int getItemCount() {
        return snakeDataList.size();
    }
    static class SnakeDataViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView confidenceTextView;
        ImageView imageView;
        TextView locationTextView;
        SnakeDataViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            confidenceTextView = itemView.findViewById(R.id.confidence_text_view);
            locationTextView=itemView.findViewById(R.id.location_text_view);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}