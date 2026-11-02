-- Read-only diagnostics: run in your Supabase SQL Editor.
-- The Android client can only see rows allowed by RLS, normally status='published'.
select difficulty, status, count(*)
from public.reference_photos
group by difficulty, status
order by difficulty, status;

-- These names are case-sensitive and must match storage_path exactly.
select name from storage.objects
where bucket_id = 'references'
order by name;

-- Rows whose referenced image has not been uploaded at the expected path.
select p.id, p.storage_path, p.status
from public.reference_photos p
left join storage.objects o
  on o.bucket_id = 'references' and o.name = p.storage_path
where o.id is null;

-- Review the policies protecting the table. Keep uploads/metadata edits admin-only.
select policyname, roles, cmd, qual, with_check
from pg_policies where schemaname = 'public' and tablename = 'reference_photos';
