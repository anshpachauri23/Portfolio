class Comment < ApplicationRecord
  # Associations
  belongs_to :submission
  belongs_to :user
  # Validations
  validates :content, presence: true
  validates :user_id, uniqueness: { scope: :submission_id, message: "You have already commented on this submission." } 
end
