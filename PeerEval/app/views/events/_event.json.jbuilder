json.extract! event, :id, :ename, :course_id, :created_at, :updated_at
json.url event_url(event, format: :json)
