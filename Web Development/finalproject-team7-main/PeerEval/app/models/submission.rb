class Submission < ApplicationRecord
  belongs_to :user, foreign_key: 'student_id'
  belongs_to :homework
  has_one_attached :file # Attached file support
  has_many :comments, dependent: :destroy 
  has_many :grades, dependent: :destroy 
  def already_commented_by?(user)
    comments.exists?(user: user)  # Check if user has commented
  end

  def already_graded_by?(user)
    grades.exists?(user: user) # Check if user has graded
  end
end
