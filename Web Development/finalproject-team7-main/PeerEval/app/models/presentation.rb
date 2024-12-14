class Presentation < ApplicationRecord
  class Presentation < ApplicationRecord
    belongs_to :presenter, class_name: 'User', foreign_key: 'presenter_id'
    
    # You can add other relationships or validations here, for example:
    validates :title, presence: true
    validates :event_date, presence: true
  end  
end
