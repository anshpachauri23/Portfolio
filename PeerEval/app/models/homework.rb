class Homework < ApplicationRecord
  belongs_to :course
  

  has_many :submissions # Associated student submissions
end
