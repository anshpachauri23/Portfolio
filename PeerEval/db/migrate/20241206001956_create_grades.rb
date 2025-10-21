class CreateGrades < ActiveRecord::Migration[7.2]
  def change
    create_table :grades do |t|
      t.integer :submission_id
      t.integer :user_id
      t.integer :grade

      t.timestamps
    end
  end
end
