class Course < ApplicationRecord
  belongs_to :instructor, class_name: 'User', foreign_key: :instructor_id # Instructor link
  
  has_many :homeworks, dependent: :destroy # Deletes homeworks when course is deleted

end
