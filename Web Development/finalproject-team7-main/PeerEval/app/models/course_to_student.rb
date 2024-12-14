class CourseToStudent < ApplicationRecord
  # Associations
  belongs_to :course
  belongs_to :student, class_name: 'User', foreign_key: :student_id #Links to user model as a student
end
