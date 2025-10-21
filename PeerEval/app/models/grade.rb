class Grade < ApplicationRecord
  belongs_to :submission
  belongs_to :user
  validates :grade, presence: true, numericality: { only_integer: true, greater_than_or_equal_to: 0, less_than_or_equal_to: 100 } # Grade must be 0-100
  validates :user_id, uniqueness: { scope: :submission_id, message: "You have already graded this submission." } # One grade per user per submission
end

