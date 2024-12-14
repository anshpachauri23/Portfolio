class AddUserIdToSubmissions < ActiveRecord::Migration[7.2]
  def change
    add_column :submissions, :user_id, :integer
  end
end
