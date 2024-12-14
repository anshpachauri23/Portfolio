class RecreateSubmissionsTable < ActiveRecord::Migration[7.0]
  def change
    create_table :submissions do |t|
      t.integer :student_id, null: false
      t.integer :homework_id, null: false
      t.string :grade
      t.text :feedback
      t.timestamps
    end
  end
end
