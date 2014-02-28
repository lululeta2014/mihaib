# Don't overwrite destination files which exist
# convert f1 -> g1 and f3 -> g3 but skip f2 -> f4 (f4 exists)
ExistingNewFile

# Force overwriting existing destination file (f4)
ExistingNewFileForce

# Don't overwrite existing .bak files
# Write f1.bak and f3.bak, but not f2.bak and f4.bak (which exist)
ExistingBakFile

# Force overwriting existing .bak files (f2.bak and f4.bak)
ExistingBakFileForce
