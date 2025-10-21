class User < ApplicationRecord
  # Relationships
  has_many :submissions, foreign_key: 'student_id', dependent: :destroy
  has_many :homeworks, through: :submissions
  has_many :comments, dependent: :destroy
  has_many :grades, dependent: :destroy
  has_many :presentations, foreign_key: 'presenter_id', dependent: :destroy

  # Devise modules for authentication
  devise :database_authenticatable, :registerable,
         :recoverable, :rememberable, :validatable



  has_many :courses, foreign_key: :instructor_id, dependent: :destroy
  has_many :course_to_students, dependent: :destroy
  has_many :enrolled_courses, through: :course_to_students, source: :course

  

 
  attr_accessor :sign_up_code

  # Constants
  ROLES = %w[student instructor].freeze

  # Validations
  validates :role, inclusion: { in: ROLES }

  # Instance Methods
  def instructor?
    role == "instructor"
  end


  def student?
    role == "student"
  end
end

