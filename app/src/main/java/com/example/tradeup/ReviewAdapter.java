package com.example.tradeup;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewVH> {
    private final List<Review> list;

    public ReviewAdapter(List<Review> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ReviewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewVH h, int pos) {
        Review r = list.get(pos);
        h.tvUserName.setText(r.getUserName());
        h.ratingBar.setRating(r.getRating());
        h.tvComment.setText(r.getComment());
        h.tvTime.setText(new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date(r.getTimestamp())));

        if (r.getAvatarBase64() != null && !r.getAvatarBase64().isEmpty()) {
            byte[] bytes = Base64.decode(r.getAvatarBase64(), Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) h.imgAvatar.setImageBitmap(bmp);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ReviewVH extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment, tvTime;
        RatingBar ratingBar;
        ImageView imgAvatar;

        public ReviewVH(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvTime = itemView.findViewById(R.id.tvTime);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
