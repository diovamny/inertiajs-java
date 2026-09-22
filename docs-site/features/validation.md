# Validation

Bean Validation failures on Inertia visits flash errors and redirect back
(`303`); precognition answers `422`. Named bags via
`X-Inertia-Error-Bag: loginForm` surface nested as
`props.errors.loginForm.<field>`. Wire shape: one message per field.
