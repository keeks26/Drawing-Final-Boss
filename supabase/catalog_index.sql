-- Optional index for the app's published-photo keyset pagination.
-- Apply once in Supabase SQL Editor; it does not publish or change any photo.
create index if not exists reference_photos_published_level_id
on public.reference_photos (difficulty, id)
where status = 'published';
